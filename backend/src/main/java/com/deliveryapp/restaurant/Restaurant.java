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
  RestaurantDetails details,
  boolean active,
  long ratingCount
) {
  public Restaurant(String id, String name, String category, String rating, String deliveryTime, int minimumOrder, String art, List<MenuItem> menu, RestaurantDetails details, boolean active) {
    this(id, name, category, rating, deliveryTime, minimumOrder, art, menu, details, active, 0);
  }
  public Restaurant(String id, String name, String category, String rating, String deliveryTime, int minimumOrder, String art, List<MenuItem> menu, RestaurantDetails details) {
    this(id, name, category, rating, deliveryTime, minimumOrder, art, menu, details, true);
  }
  public Restaurant(String id, String name, String category, String rating, String deliveryTime, int minimumOrder, String art, List<MenuItem> menu) {
    this(id, name, category, rating, deliveryTime, minimumOrder, art, menu, null);
  }
}
