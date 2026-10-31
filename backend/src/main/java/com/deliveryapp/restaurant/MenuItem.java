package com.deliveryapp.restaurant;

public record MenuItem(
  String id,
  String name,
  String description,
  int price,
  String photoUrl,
  String category
) {
  public MenuItem(String id, String name, String description, int price, String photoUrl) { this(id, name, description, price, photoUrl, null); }
  public MenuItem(String id, String name, String description, int price) { this(id, name, description, price, null, null); }
}
