package com.deliveryapp.restaurant;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantFeedbackController {
  private final RestaurantRatingService service;
  public RestaurantFeedbackController(RestaurantRatingService service) { this.service = service; }
  @GetMapping("/{id}/feedback")
  public RestaurantRatingService.FeedbackPage feedback(@PathVariable String id, @RequestParam(defaultValue = "0") int page) {
    return service.publicFeedback(id, page);
  }
}
