package com.deliveryapp.order;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
  String id,
  OrderStatus status,
  String customerPhone,
  String customerAddress,
  String area,
  List<OrderItemResponse> items,
  int subtotal,
  int deliveryFee,
  int total,
  Instant createdAt
) {}
