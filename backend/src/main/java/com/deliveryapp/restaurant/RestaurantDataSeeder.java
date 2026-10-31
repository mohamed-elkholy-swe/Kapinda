package com.deliveryapp.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.springframework.core.annotation.Order(0)
public class RestaurantDataSeeder implements CommandLineRunner {
  private final RestaurantRepository restaurantRepository;
  private final ObjectMapper mapper;

  public RestaurantDataSeeder(RestaurantRepository restaurantRepository, ObjectMapper mapper) {
    this.restaurantRepository = restaurantRepository;
    this.mapper = mapper;
  }

  public record Listing(String id, String name, String category, String rating, String art, RestaurantDetails details, java.util.List<MenuItem> menu) {}

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    try (var input = new ClassPathResource("data/qanater-restaurants.json").getInputStream()) {
      for (Listing listing : mapper.readValue(input, Listing[].class)) {
        var existing = restaurantRepository.findById(listing.id());
        var restaurant = existing.orElseGet(() -> new RestaurantEntity(listing.id(), listing.name(), listing.category(),
          listing.rating(), "غير مؤكد", 0, listing.art()));
        var details = restaurant.getDetails();
        if (details != null && details.catalogVersion != null && details.catalogVersion >= 2) continue;
        if (details == null) {
          details = listing.details();
          restaurant.setDetails(details);
        } else {
          // Enrich the original import without replacing contact details or later admin menu edits.
          details.photoUrl = listing.details().photoUrl;
          details.photoSourceUrl = listing.details().photoSourceUrl;
          details.menuImages = listing.details().menuImages;
          details.menuSourceUrl = listing.details().menuSourceUrl;
          details.menuNote = listing.details().menuNote;
          details.catalogVersion = 2;
        }
        // Only populate an empty menu; existing admin menus remain authoritative.
        if (restaurant.getMenu().isEmpty() && listing.menu() != null) {
          for (MenuItem item : listing.menu()) {
            restaurant.addMenuItem(new MenuItemEntity(item.id(), item.name(), item.description(), item.price()));
          }
        }
        restaurantRepository.save(restaurant);
      }
    }
  }
}
