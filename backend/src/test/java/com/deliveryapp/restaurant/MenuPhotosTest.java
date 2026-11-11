package com.deliveryapp.restaurant;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuPhotosTest {
  private SaveRestaurantRequest request(List<String> images) {
    return new SaveRestaurantRequest("Kitchen", "food", "30 min", 0, "restaurant", null, null, null, null, images);
  }

  @Test void savesPhotoOrderPreservesOmittedPhotosAndSupportsRemovingAllPhotos() {
    var repository = mock(RestaurantRepository.class);
    var entity = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    when(repository.findById("r")).thenReturn(Optional.of(entity));
    var service = new RestaurantService(repository, mock(MenuItemRepository.class), mock(RestaurantCategoryService.class));
    var images = List.of("https://example.com/menu-2.jpg", "https://example.com/menu-1.jpg");
    assertEquals(images, service.update("r", request(images)).details().menuImages);
    assertEquals(images, service.update("r", request(null)).details().menuImages);
    assertTrue(service.update("r", request(List.of())).details().menuImages.isEmpty());
  }

  @Test void validatesSecurePhotoUrlsAndLimits() {
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertTrue(validator.validate(request(null)).isEmpty());
      assertTrue(validator.validate(request(List.of())).isEmpty());
      assertTrue(validator.validate(request(List.of("https://example.com/menu.jpg"))).isEmpty());
      assertFalse(validator.validate(request(List.of("http://example.com/menu.jpg"))).isEmpty());
      assertFalse(validator.validate(request(List.of("javascript:alert(1)"))).isEmpty());
      assertFalse(validator.validate(request(List.of(""))).isEmpty());
      assertFalse(validator.validate(request(java.util.Collections.nCopies(31, "https://example.com/menu.jpg"))).isEmpty());
      assertFalse(validator.validate(request(List.of("https://example.com/" + "a".repeat(1500)))).isEmpty());
    }
  }
}
