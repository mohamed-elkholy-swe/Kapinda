package com.deliveryapp.restaurant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Remove automatically generated demo dishes, preserving manually entered dishes. */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.sample-menu-cleanup.enabled", havingValue = "true")
@Order(100)
public class SampleMenuSeeder implements CommandLineRunner {
  private final RestaurantRepository repository;
  public SampleMenuSeeder(RestaurantRepository repository) { this.repository = repository; }
  @Override @Transactional
  public void run(String... args) {
    for (RestaurantEntity restaurant : repository.findAll()) {
      var samples = restaurant.getMenu().stream()
        .filter(item -> item.getId().matches(java.util.regex.Pattern.quote(restaurant.getId()) + "-sample-v1-[1-6]"))
        .toList();
      samples.forEach(restaurant::removeMenuItem);
    }
  }
}
