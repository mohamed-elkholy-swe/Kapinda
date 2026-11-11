package com.deliveryapp.restaurant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

class RestaurantSummaryTest {
  @Test void summaryRouteReturnsCardsWhileDetailRouteStillReturnsTheMenu() throws Exception {
    var service = mock(RestaurantService.class);
    var card = new RestaurantSummary("r", "Kitchen", "food", "4.8", "30 min", 0,
      "restaurant", 2L, "https://example.com/photo.jpg", true);
    when(service.findPublicSummaries("pizza")).thenReturn(List.of(card));
    var dish = new MenuItem("dish", "Pizza", "", 50);
    when(service.findPublicById("r")).thenReturn(new Restaurant("r", "Kitchen", "food", "4.8", "30 min", 0, "restaurant", List.of(dish)));
    var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
      .standaloneSetup(new RestaurantController(service)).build();
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/restaurants/summaries").param("search", "pizza"))
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].photoUrl").value(card.photoUrl()))
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].menu").doesNotExist());
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/restaurants/r"))
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.menu[0].name").value("Pizza"));
  }

  @Test void summaryQueryIsValidAgainstTheActualEntityMappings() throws Exception {
    // Parse and type-check HQL without connecting to or changing a database.
    var configuration = new org.hibernate.cfg.Configuration()
      .addAnnotatedClass(RestaurantEntity.class)
      .addAnnotatedClass(MenuItemEntity.class)
      .addAnnotatedClass(RestaurantRatingEntity.class)
      .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
      .setProperty("hibernate.boot.allow_jdbc_metadata_access", "false")
      .setProperty("hibernate.hbm2ddl.auto", "none");
    String hql = RestaurantRepository.class.getMethod("findPublicSummaries", String.class)
      .getAnnotation(Query.class).value();
    try (var factory = configuration.buildSessionFactory(); var session = factory.openSession()) {
      var query = session.createQuery(hql, RestaurantSummary.class);
      query.setParameter("search", "pizza");
      assertEquals("pizza", query.getParameterValue("search"));
    }
  }

  @Test void summariesUseTheProjectionWithoutLoadingRestaurantEntities() throws Exception {
    var repository = mock(RestaurantRepository.class);
    var summary = new RestaurantSummary("r", "Kitchen", "food", "4.8", "30 min", 0,
      "restaurant", null, "https://example.com/photo.jpg", null);
    when(repository.findPublicSummaries("pizza")).thenReturn(List.of(summary));
    var service = new RestaurantService(repository, mock(MenuItemRepository.class), mock(RestaurantCategoryService.class));
    assertEquals(List.of(summary), service.findPublicSummaries("  PIZZA  "));
    verify(repository).findPublicSummaries("pizza");
    verifyNoMoreInteractions(repository);
    var json = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(summary);
    assertFalse(json.has("menu"));
    assertFalse(json.has("details"));
    assertEquals("New", json.get("rating").asText());
    assertEquals(0, json.get("ratingCount").asInt());
  }
}
