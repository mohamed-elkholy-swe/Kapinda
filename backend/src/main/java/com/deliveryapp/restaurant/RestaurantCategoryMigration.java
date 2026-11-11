package com.deliveryapp.restaurant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(20)
public class RestaurantCategoryMigration implements CommandLineRunner {
  private final RestaurantRepository restaurants;
  private final RestaurantCategoryService categories;
  public RestaurantCategoryMigration(RestaurantRepository restaurants, RestaurantCategoryService categories) { this.restaurants = restaurants; this.categories = categories; }
  @Override @Transactional
  public void run(String... args) {
    for (var restaurant : restaurants.findAll()) restaurant.setCategory(categories.importExisting(restaurant.getCategory()));
  }
}
