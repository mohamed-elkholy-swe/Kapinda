package com.deliveryapp.restaurant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SaveRestaurantRequest(
  @NotBlank String name,
  @NotBlank String category,
  @NotBlank String deliveryTime,
  @Min(0) int minimumOrder,
  @NotBlank String art,
  @jakarta.validation.constraints.Size(max = 2000) @org.hibernate.validator.constraints.URL(protocol = "https") String photoUrl
) {
  public SaveRestaurantRequest(String name, String category, String deliveryTime, int minimumOrder, String art) { this(name, category, deliveryTime, minimumOrder, art, null); }
}
