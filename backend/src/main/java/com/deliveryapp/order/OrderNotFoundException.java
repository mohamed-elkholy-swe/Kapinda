package com.deliveryapp.order;

public class OrderNotFoundException extends RuntimeException {
  public OrderNotFoundException(String id) {
    super("Order not found: " + id);
  }
}
