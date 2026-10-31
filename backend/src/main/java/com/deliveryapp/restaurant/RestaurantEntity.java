package com.deliveryapp.restaurant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class RestaurantEntity {
  @Id
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private String rating;

  @Column(name = "delivery_time", nullable = false)
  private String deliveryTime;

  @Column(name = "minimum_order", nullable = false)
  private int minimumOrder;

  @Column(nullable = false)
  private String art;

  @Column(name = "owner_uid", unique = true)
  private String ownerUid;

  @Enumerated(EnumType.STRING)
  @Column(name = "approval_status")
  private RestaurantApprovalStatus approvalStatus = RestaurantApprovalStatus.APPROVED;

  @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<MenuItemEntity> menu = new ArrayList<>();

  @jakarta.persistence.Embedded
  private RestaurantDetails details;

  public RestaurantDetails getDetails() { return details; }

  public void setDetails(RestaurantDetails details) { this.details = details; }

  private Integer sampleMenuVersion;
  public Integer getSampleMenuVersion() { return sampleMenuVersion; }
  public void setSampleMenuVersion(Integer value) { sampleMenuVersion = value; }

  protected RestaurantEntity() {}

  public RestaurantEntity(
    String id,
    String name,
    String category,
    String rating,
    String deliveryTime,
    int minimumOrder,
    String art
  ) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.rating = rating;
    this.deliveryTime = deliveryTime;
    this.minimumOrder = minimumOrder;
    this.art = art;
  }

  public void addMenuItem(MenuItemEntity item) {
    item.setRestaurant(this);
    menu.add(item);
  }

  public void removeMenuItem(MenuItemEntity item) {
    menu.remove(item);
  }

  public void update(String name, String category, String deliveryTime, int minimumOrder, String art) {
    this.name = name;
    this.category = category;
    this.deliveryTime = deliveryTime;
    this.minimumOrder = minimumOrder;
    this.art = art;
  }

  public void setOwnerUid(String ownerUid) {
    this.ownerUid = ownerUid;
  }

  public void setApprovalStatus(RestaurantApprovalStatus approvalStatus) {
    this.approvalStatus = approvalStatus;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCategory() {
    return category;
  }

  public String getRating() {
    return rating;
  }

  public String getDeliveryTime() {
    return deliveryTime;
  }

  public int getMinimumOrder() {
    return minimumOrder;
  }

  public String getArt() {
    return art;
  }

  public String getOwnerUid() {
    return ownerUid;
  }

  public RestaurantApprovalStatus getApprovalStatus() {
    return approvalStatus;
  }

  public List<MenuItemEntity> getMenu() {
    return menu;
  }
}
