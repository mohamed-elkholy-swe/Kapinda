package com.deliveryapp.restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_items")
public class MenuItemEntity {
  @Id
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private int price;

  @Column(length = 2000)
  private String photoUrl;
  public String getPhotoUrl() { return photoUrl; }
  public void setPhotoUrl(String value) { photoUrl = value; }

  @Column(length = 100)
  private String category;
  public String getCategory() { return category; }
  public void setCategory(String value) { category = value; }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "restaurant_id", nullable = false)
  private RestaurantEntity restaurant;

  protected MenuItemEntity() {}

  public MenuItemEntity(String id, String name, String description, int price) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getPrice() {
    return price;
  }

  public RestaurantEntity getRestaurant() {
    return restaurant;
  }

  public void setRestaurant(RestaurantEntity restaurant) {
    this.restaurant = restaurant;
  }

  public void update(String name, String description, int price) {
    this.name = name;
    this.description = description;
    this.price = price;
  }
}
