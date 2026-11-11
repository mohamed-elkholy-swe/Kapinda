package com.deliveryapp.restaurant;

import java.util.*;
import com.deliveryapp.account.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RestaurantCategoryTest {
  private final Map<String, RestaurantCategoryEntity> saved = new HashMap<>();
  private final RestaurantCategoryRepository repository = mock(RestaurantCategoryRepository.class);
  private final RestaurantCategoryService service = new RestaurantCategoryService(repository);

  RestaurantCategoryTest() {
    when(repository.findById(anyString())).thenAnswer(inv -> Optional.ofNullable(saved.get(inv.getArgument(0))));
    when(repository.findByNameArKeyOrNameEnKey(anyString(), anyString())).thenAnswer(inv -> saved.values().stream()
      .filter(c -> RestaurantCategoryService.normalize(c.getNameAr()).equals(inv.getArgument(0))
        || c.getNameEn() != null && RestaurantCategoryService.normalize(c.getNameEn()).equals(inv.getArgument(1))).findFirst());
    when(repository.save(any())).thenAnswer(inv -> { RestaurantCategoryEntity category = inv.getArgument(0); saved.put(category.getId(), category); return category; });
    when(repository.findAll()).thenAnswer(inv -> new ArrayList<>(saved.values()));
  }
  @Test void mergesArabicEnglishAndSpellingVariantsWithoutCreatingStarterCategories() {
    assertTrue(service.list().isEmpty());
    assertEquals("pizza", service.importExisting(" Pizza "));
    assertEquals("pizza", service.importExisting("بِيتـزا"));
    assertEquals(1, service.list().size());
    assertEquals("بيتزا", service.list().get(0).nameAr());
    assertEquals("Pizza", service.list().get(0).nameEn());
    assertEquals("pizza", service.create(new RestaurantCategoryService.SaveCategory("بيتزا", "Pizzas")).id());
    assertEquals(1, service.list().size());
  }
  @Test void savesOptionalEnglishAndRejectsUnknownRestaurantCategories() {
    var category = service.create(new RestaurantCategoryService.SaveCategory("مأكولات بحرية", null));
    assertNull(category.nameEn());
    assertEquals(category.id(), service.requireId(category.id()));
    assertEquals(category.id(), service.create(new RestaurantCategoryService.SaveCategory("  مأكولات   بحرية ", "")).id());
    var bilingual = service.create(new RestaurantCategoryService.SaveCategory("سوشي", "Sushi"));
    assertEquals(bilingual.id(), service.requireId("SUSHI"));
    assertThrows(ResponseStatusException.class, () -> service.requireId("unknown"));
    assertThrows(ResponseStatusException.class, () -> service.create(new RestaurantCategoryService.SaveCategory("مأكولات بحرية", "Sushi")));
  }
  @Test void migrationIsIdempotentAndPreservesMenuAndRestaurantDetails() {
    var restaurants = mock(RestaurantRepository.class);
    var a = new RestaurantEntity("a", "A", "pizza", "New", "30", 0, "pizza");
    var b = new RestaurantEntity("b", "B", "بيتزا", "New", "40", 10, "pizza");
    b.addMenuItem(new MenuItemEntity("dish", "Dish", "", 20));
    when(restaurants.findAll()).thenReturn(List.of(a,b));
    var migration = new RestaurantCategoryMigration(restaurants, service);
    migration.run(); migration.run();
    assertEquals("pizza", a.getCategory()); assertEquals("pizza", b.getCategory());
    assertEquals("B", b.getName()); assertEquals(1, b.getMenu().size()); assertEquals(1, saved.size());
  }
  @Test void validatesRequiredArabicAndOptionalEnglish() {
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertFalse(validator.validate(new RestaurantCategoryService.SaveCategory(" ", "Pizza")).isEmpty());
      assertTrue(validator.validate(new RestaurantCategoryService.SaveCategory("بيتزا", null)).isEmpty());
      assertFalse(validator.validate(new RestaurantCategoryService.SaveCategory("ا".repeat(101), null)).isEmpty());
    }
  }
  @Test void restaurantCreationStoresTheSelectedCategoryIdAndRejectsFreeText() {
    var restaurants = mock(RestaurantRepository.class);
    when(restaurants.save(any())).thenAnswer(inv -> inv.getArgument(0));
    var restaurantService = new RestaurantService(restaurants, mock(MenuItemRepository.class), service);
    var category = service.create(new RestaurantCategoryService.SaveCategory("سوشي", "Sushi"));
    var result = restaurantService.create(new SaveRestaurantRequest("Kitchen", category.id(), "30", 0, "restaurant"));
    assertEquals(category.id(), result.category());
    assertThrows(ResponseStatusException.class, () -> restaurantService.create(new SaveRestaurantRequest("Other", "unsaved category", "30", 0, "restaurant")));
  }
  @Test void creationUsesProtectedRoleChecksAndReadIsPublic() {
    var accounts = mock(AccountService.class);
    var controller = new RestaurantCategoryController(service, accounts);
    assertTrue(controller.list().isEmpty()); verifyNoInteractions(accounts);
    var user = new AuthenticatedUser("staff", "staff@example.test");
    var request = new RestaurantCategoryService.SaveCategory("بيتزا", "Pizza");
    doThrow(new SecurityException("Access denied")).when(accounts).requireRole(user, AccountRole.ADMIN);
    assertThrows(SecurityException.class, () -> controller.create(user, request));
    assertTrue(saved.isEmpty());
  }
}
