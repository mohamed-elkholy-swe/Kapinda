package com.deliveryapp.restaurant;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_categories")
public class RestaurantCategoryEntity {
  @Id private String id;
  @Column(nullable = false, length = 100) private String nameAr;
  @Column(length = 100) private String nameEn;
  @Column(nullable = false, unique = true, length = 100) private String nameArKey;
  @Column(unique = true, length = 100) private String nameEnKey;
  protected RestaurantCategoryEntity() {}
  public RestaurantCategoryEntity(String id, String nameAr, String nameEn) {
    this.id = id; this.nameAr = nameAr.trim();
    this.nameEn = nameEn == null || nameEn.isBlank() ? null : nameEn.trim();
    this.nameArKey = RestaurantCategoryService.normalize(this.nameAr);
    this.nameEnKey = this.nameEn == null ? null : RestaurantCategoryService.normalize(this.nameEn);
  }
  public String getId() { return id; }
  public String getNameAr() { return nameAr; }
  public String getNameEn() { return nameEn; }
}
