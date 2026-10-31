package com.deliveryapp.restaurant;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
  private final RestaurantService restaurantService;

  public RestaurantController(RestaurantService restaurantService) {
    this.restaurantService = restaurantService;
  }

  @GetMapping
  public List<Restaurant> findAll(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String search
  ) {
    return restaurantService.findAll(category, search);
  }

  @GetMapping("/{id}")
  public Restaurant findById(@PathVariable String id) {
    return restaurantService.findById(id);
  }
}
