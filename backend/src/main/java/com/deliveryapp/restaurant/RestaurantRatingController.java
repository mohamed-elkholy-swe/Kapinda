package com.deliveryapp.restaurant;

import com.deliveryapp.account.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account/restaurant-ratings")
public class RestaurantRatingController {
  private final RestaurantRatingService service;
  public RestaurantRatingController(RestaurantRatingService service) { this.service = service; }

  @GetMapping("/{id}")
  public RestaurantRatingService.RatingResponse mine(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String id) {
    return service.mine(authenticatedUser, id);
  }

  @PutMapping("/{id}")
  public RestaurantRatingService.RatingResponse rate(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String id,
      @Valid @RequestBody RestaurantRatingService.RatingRequest request) {
    return service.rate(authenticatedUser, id, request.stars());
  }
}
