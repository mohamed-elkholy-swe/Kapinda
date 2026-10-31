package com.deliveryapp.restaurant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SaveMenuItemRequest(
  @NotBlank String name,
  String description,
  @Min(1) int price,
  @jakarta.validation.constraints.Size(max = 2000) @org.hibernate.validator.constraints.URL(protocol = "https") String photoUrl,
  @jakarta.validation.constraints.Size(max = 100) String category
) {
  public SaveMenuItemRequest(String name, String description, int price, String photoUrl) { this(name, description, price, photoUrl, null); }
  public SaveMenuItemRequest(String name, String description, int price) { this(name, description, price, null, null); }
}
