package com.deliveryapp.restaurant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RestaurantPhotoTest {
  @Test void categoriesCanBeCreatedBeforeItemsAndItemsCanRemainUncategorised() {
    var repository = mock(RestaurantRepository.class);
    var menu = mock(MenuItemRepository.class);
    var service = new RestaurantService(repository, menu);
    var entity = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    when(repository.findById("r")).thenReturn(Optional.of(entity));
    service.addMenuCategory("r", " Pizza ");
    service.addMenuCategory("r", "pizza");
    assertEquals(java.util.List.of("Pizza"), entity.getDetails().menuCategories);
    assertTrue(entity.getMenu().isEmpty());
    var grouped = service.addMenuItem("r", new SaveMenuItemRequest("Dish", "Description", 50, null, "Pizza"));
    assertEquals("Pizza", grouped.category());
    var direct = service.addMenuItem("r", new SaveMenuItemRequest("Direct", "Description", 50));
    assertNull(direct.category());
    var stored = entity.getMenu().stream().filter(item -> item.getId().equals(grouped.id())).findFirst().orElseThrow();
    when(menu.findById(grouped.id())).thenReturn(Optional.of(stored));
    var moved = service.updateMenuItem("r", grouped.id(), new SaveMenuItemRequest("Dish", "Description", 50, null, ""));
    assertEquals("", moved.category());
  }

  @Test void photoEditsPreserveOtherRestaurantDetailsAndCanBeCleared() {
    var repository = mock(RestaurantRepository.class);
    var service = new RestaurantService(repository, mock(MenuItemRepository.class));
    var entity = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    var details = new RestaurantDetails();
    details.address = "Existing address";
    details.photoUrl = "https://example.com/old.jpg";
    entity.setDetails(details);
    when(repository.findById("r")).thenReturn(Optional.of(entity));
    var result = service.update("r", new SaveRestaurantRequest("Kitchen", "food", "30 min", 0, "restaurant", "https://example.com/new.jpg"));
    assertEquals("https://example.com/new.jpg", result.details().photoUrl);
    assertEquals("Existing address", result.details().address);
    service.update("r", new SaveRestaurantRequest("Kitchen", "food", "30 min", 0, "restaurant"));
    assertEquals("https://example.com/new.jpg", details.photoUrl);
    service.update("r", new SaveRestaurantRequest("Kitchen", "food", "30 min", 0, "restaurant", ""));
    assertEquals("", details.photoUrl);
  }

  @Test void menuPhotoIsReturnedAndUnsafeLinksAreRejected() {
    var repository = mock(RestaurantRepository.class);
    var menu = mock(MenuItemRepository.class);
    var service = new RestaurantService(repository, menu);
    var entity = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    when(repository.findById("r")).thenReturn(Optional.of(entity));
    var result = service.addMenuItem("r", new SaveMenuItemRequest("Dish", "Description", 50, "https://example.com/dish.jpg"));
    assertEquals("https://example.com/dish.jpg", result.photoUrl());
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertFalse(validator.validate(new SaveMenuItemRequest("Dish", "Description", 50, "javascript:alert(1)")).isEmpty());
      assertTrue(validator.validate(new SaveMenuItemRequest("Dish", "Description", 50, "")).isEmpty());
    }
  }
}
