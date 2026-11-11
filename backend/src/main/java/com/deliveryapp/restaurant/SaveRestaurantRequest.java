package com.deliveryapp.restaurant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SaveRestaurantRequest(
  @NotBlank String name,
  @NotBlank String category,
  @NotBlank String deliveryTime,
  @Min(0) int minimumOrder,
  @NotBlank String art,
  @jakarta.validation.constraints.Size(max = 2000) @org.hibernate.validator.constraints.URL(protocol = "https") String photoUrl,
  @jakarta.validation.constraints.Size(max = 1000) String address,
  @jakarta.validation.constraints.Pattern(regexp = "^$|([01][0-9]|2[0-3]):[0-5][0-9]") String openingTime,
  @jakarta.validation.constraints.Pattern(regexp = "^$|([01][0-9]|2[0-3]):[0-5][0-9]") String closingTime,
  @jakarta.validation.constraints.Size(max = 30) java.util.List<@NotBlank @jakarta.validation.constraints.Size(max = 1500) @org.hibernate.validator.constraints.URL(protocol = "https") String> menuImages
) {
  public SaveRestaurantRequest(String name, String category, String deliveryTime, int minimumOrder, String art) { this(name, category, deliveryTime, minimumOrder, art, null, null, null, null, null); }
  public SaveRestaurantRequest(String name, String category, String deliveryTime, int minimumOrder, String art, String photoUrl) { this(name, category, deliveryTime, minimumOrder, art, photoUrl, null, null, null, null); }
  public SaveRestaurantRequest(String name, String category, String deliveryTime, int minimumOrder, String art, String photoUrl, String address, String openingTime, String closingTime) { this(name, category, deliveryTime, minimumOrder, art, photoUrl, address, openingTime, closingTime, null); }
}
