package com.deliveryapp.restaurant;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantDetailsTest {
  @Test void savesAddressAndOvernightHoursWithoutReplacingOtherDetails() {
    var repository = mock(RestaurantRepository.class);
    var entity = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    var details = new RestaurantDetails();
    details.phone = "12345";
    entity.setDetails(details);
    when(repository.findById("r")).thenReturn(Optional.of(entity));
    var service = new RestaurantService(repository, mock(MenuItemRepository.class), mock(RestaurantCategoryService.class));
    var result = service.update("r", new SaveRestaurantRequest("Kitchen", "food", "30 min", 0, "restaurant", null, " Main street ", "18:00", "02:00"));
    assertEquals("Main street", result.details().address);
    assertEquals("18:00", result.details().openingTime);
    assertEquals("02:00", result.details().closingTime);
    assertEquals("12345", result.details().phone);
  }

  @Test void removesOnlyGeneratedSamplesAndNeverAddsDishes() {
    var repository = mock(RestaurantRepository.class);
    var entity = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    entity.addMenuItem(new MenuItemEntity("r-sample-v1-1", "Sample", "Demo", 10));
    entity.addMenuItem(new MenuItemEntity("manual", "Real dish", "", 20));
    when(repository.findAll()).thenReturn(List.of(entity));
    var cleanup = new SampleMenuSeeder(repository);
    cleanup.run();
    cleanup.run();
    assertEquals(1, entity.getMenu().size());
    assertEquals("manual", entity.getMenu().get(0).getId());
  }
}
