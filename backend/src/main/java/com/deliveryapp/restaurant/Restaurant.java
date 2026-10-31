package com.deliveryapp.restaurant;

import java.util.List;

public record Restaurant(
  String id,
  String name,
  String category,
  String rating,
  String deliveryTime,
  int minimumOrder,
  String art,
  List<MenuItem> menu,
  RestaurantDetails details
) {
  public Restaurant(String id, String name, String category, String rating, String deliveryTime, int minimumOrder, String art, List<MenuItem> menu) {
    this(id, name, category, rating, deliveryTime, minimumOrder, art, menu, null);
  }
}
