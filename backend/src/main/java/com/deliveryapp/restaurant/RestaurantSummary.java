package com.deliveryapp.restaurant;

/** Scalar projection: listing queries never hydrate restaurant entities or menus. */
public record RestaurantSummary(String id, String name, String category, String rating,
    String deliveryTime, int minimumOrder, String art, Long ratingCount,
    String photoUrl, Boolean orderingTermsVerified) {
  public RestaurantSummary {
    ratingCount = ratingCount == null ? 0L : ratingCount;
    if (ratingCount == 0) rating = "New";
  }
}
