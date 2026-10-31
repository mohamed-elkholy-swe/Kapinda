package com.deliveryapp.restaurant;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SampleMenuSeederTest {
  @Test void addsThreeCategoriesPhotosAndSixSamplesToEveryRestaurantOnce() {
    var repository = mock(RestaurantRepository.class);
    var restaurants = List.of(
      new RestaurantEntity("pizza", "Pizza", "pizza", "4", "30", 0, "pizza"),
      new RestaurantEntity("grill", "Grill", "grill", "4", "30", 0, "grill"),
      new RestaurantEntity("sweet", "Sweet", "dessert", "4", "30", 0, "dessert"),
      new RestaurantEntity("burger", "Burger", "fast", "4", "30", 0, "burger"));
    for (var restaurant : restaurants) restaurant.addMenuItem(new MenuItemEntity(restaurant.getId()+"-old", "بطاطس", "Existing", 25));
    when(repository.findAll()).thenReturn(restaurants);
    var seeder = new SampleMenuSeeder(repository);
    seeder.run();
    for (var restaurant : restaurants) {
      assertEquals(7, restaurant.getMenu().size());
      assertEquals(3, restaurant.getMenu().stream().map(MenuItemEntity::getCategory).distinct().count());
      assertTrue(restaurant.getMenu().stream().allMatch(item -> item.getPhotoUrl().startsWith("https://images.unsplash.com/")));
      assertEquals(25, restaurant.getMenu().get(0).getPrice());
    }
    seeder.run();
    for (var restaurant : restaurants) assertEquals(7, restaurant.getMenu().size());
    verify(repository, times(4)).save(any());
  }
  @Test void preservesExistingItemPhotosAndCategories() {
    var repository=mock(RestaurantRepository.class);
    var restaurant=new RestaurantEntity("custom", "Custom", "grill", "4", "30", 0, "grill");
    var item=new MenuItemEntity("custom-item", "Custom dish", "Original", 99);
    item.setCategory("My category"); item.setPhotoUrl("https://example.com/custom.jpg");
    restaurant.addMenuItem(item);
    when(repository.findAll()).thenReturn(List.of(restaurant));
    new SampleMenuSeeder(repository).run();
    assertEquals("My category",item.getCategory());
    assertEquals("https://example.com/custom.jpg",item.getPhotoUrl());
  }
}
