package com.deliveryapp.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import java.util.Optional;

class RestaurantDataSeederTest {
  @Test void importsPhotosMenusAndDoesNotRepeatAnImport() throws Exception {
    var repository = mock(RestaurantRepository.class);
    var seeder = new RestaurantDataSeeder(repository, new ObjectMapper());
    seeder.run();
    var saved = ArgumentCaptor.forClass(RestaurantEntity.class);
    verify(repository, times(4)).save(saved.capture());
    assertEquals(4, saved.getAllValues().stream().map(RestaurantEntity::getId).distinct().count());
    assertEquals(68, saved.getAllValues().stream().mapToInt(r -> r.getMenu().size()).sum());
    for (var restaurant : saved.getAllValues()) {
      assertFalse(restaurant.getMenu().isEmpty());
      assertFalse(restaurant.getDetails().orderingTermsVerified);
      assertNotNull(restaurant.getDetails().photoUrl);
      assertFalse(restaurant.getDetails().menuImages.isEmpty());
      assertNotNull(restaurant.getDetails().menuSourceUrl);
      when(repository.findById(restaurant.getId())).thenReturn(Optional.of(restaurant));
    }
    clearInvocations(repository);
    seeder.run();
    verify(repository, never()).save(any());
    // A later admin deletion must not get re-imported on the next startup.
    saved.getAllValues().get(0).getMenu().clear();
    seeder.run();
    assertTrue(saved.getAllValues().get(0).getMenu().isEmpty());
  }

  @Test void enrichesOldImportsAndPreservesAdminContactAndMenuEdits() throws Exception {
    var repository = mock(RestaurantRepository.class);
    var restaurant = new RestaurantEntity("el-galad-grill", "Admin name", "grill", "3.9", "45 min", 50, "grill");
    var details = new RestaurantDetails();
    details.phone = "01100000000";
    restaurant.setDetails(details);
    restaurant.addMenuItem(new MenuItemEntity("admin-item", "Admin dish", "Updated", 500));
    when(repository.findById(restaurant.getId())).thenReturn(Optional.of(restaurant));
    new RestaurantDataSeeder(repository, new ObjectMapper()).run();
    assertEquals("Admin name", restaurant.getName());
    assertEquals("01100000000", restaurant.getDetails().phone);
    assertNotNull(restaurant.getDetails().photoUrl);
    assertEquals(5, restaurant.getDetails().menuImages.size());
    assertEquals(1, restaurant.getMenu().size());
    assertEquals("admin-item", restaurant.getMenu().get(0).getId());
  }

  @Test void imageListsRoundTripIncludingNullLegacyRows() {
    var converter = new StringListConverter();
    var urls = java.util.List.of("https://example.com/a.jpg?x=1&y=2");
    assertEquals(urls, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(urls)));
    assertTrue(converter.convertToEntityAttribute(null).isEmpty());
  }
}
