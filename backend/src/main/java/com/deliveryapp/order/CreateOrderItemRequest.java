package com.deliveryapp.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderItemRequest(
  @NotBlank String restaurantId,
  @NotBlank String menuItemId,
  @Min(1) int quantity
) {}
