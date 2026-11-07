package com.deliveryapp.restaurant;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"restaurant_id", "customer_uid"}))
public class RestaurantRatingEntity {
  @Id
  private String id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "restaurant_id", nullable = false)
  private RestaurantEntity restaurant;
  @Column(name = "customer_uid", nullable = false)
  private String customerUid;
  @Column(nullable = false)
  private int stars;

  protected RestaurantRatingEntity() {}
  public RestaurantRatingEntity(RestaurantEntity restaurant, String customerUid, int stars) {
    this.id = java.util.UUID.randomUUID().toString();
    this.restaurant = restaurant;
    this.customerUid = customerUid;
    this.stars = stars;
  }
  public int getStars() { return stars; }
  public void setStars(int stars) { this.stars = stars; }
}
