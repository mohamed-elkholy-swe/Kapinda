package com.deliveryapp.restaurant;

public class MenuItemNotFoundException extends RuntimeException {
  public MenuItemNotFoundException(String id) {
    super("Menu item not found: " + id);
  }
}
