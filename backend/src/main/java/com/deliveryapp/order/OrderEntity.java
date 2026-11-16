package com.deliveryapp.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(name = "customer_phone", nullable = false)
  private String customerPhone;

  @Column(name = "customer_name", length = 100)
  private String customerName;

  @Column(name = "customer_uid")
  private String customerUid;

  @Column(name = "delivery_person_uid")
  private String deliveryPersonUid;

  @Column(name = "customer_address", nullable = false)
  private String customerAddress;

  @Column(nullable = false)
  private String area;

  @Column(nullable = false)
  private int subtotal;

  @Column(name = "delivery_fee", nullable = false)
  private int deliveryFee;

  @Column(nullable = false)
  private int total;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItemEntity> items = new ArrayList<>();

  protected OrderEntity() {}

  public OrderEntity(
    OrderStatus status,
    String customerUid,
    String customerPhone,
    String customerAddress,
    String area,
    int subtotal,
    int deliveryFee,
    int total,
    Instant createdAt
  ) {
    this.status = status;
    this.customerUid = customerUid;
    this.customerPhone = customerPhone;
    this.customerAddress = customerAddress;
    this.area = area;
    this.subtotal = subtotal;
    this.deliveryFee = deliveryFee;
    this.total = total;
    this.createdAt = createdAt;
  }

  public void addItem(OrderItemEntity item) {
    item.setOrder(this);
    items.add(item);
  }

  public Long getId() {
    return id;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public String getCustomerPhone() {
    return customerPhone;
  }

  public String getCustomerName() { return customerName; }
  public void setCustomerName(String name) { this.customerName = name; }

  public String getCustomerUid() {
    return customerUid;
  }
  public String getDeliveryPersonUid() { return deliveryPersonUid; }
  public void assignDeliveryPerson(String uid) { this.deliveryPersonUid = uid; }

  public String getCustomerAddress() {
    return customerAddress;
  }

  public String getArea() {
    return area;
  }

  public int getSubtotal() {
    return subtotal;
  }

  public int getDeliveryFee() {
    return deliveryFee;
  }

  public int getTotal() {
    return total;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<OrderItemEntity> getItems() {
    return items;
  }

  public void setStatus(OrderStatus status) {
    items.forEach(item -> item.setRestaurantStatus(item.getRestaurantStatus()));
    this.status = status;
    if (java.util.Set.of(OrderStatus.PLACED, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY).contains(status)) {
      items.forEach(item -> item.setRestaurantStatus(status));
    }
  }

  public void updateRestaurantStatus(String restaurantId, OrderStatus next) {
    if (!java.util.Set.of(OrderStatus.PLACED, OrderStatus.ACCEPTED, OrderStatus.PREPARING).contains(status))
      throw new IllegalArgumentException("This order is no longer open for kitchen updates");
    var selected = items.stream().filter(item -> item.getRestaurantId().equals(restaurantId)).toList();
    if (selected.isEmpty()) throw new IllegalArgumentException("Restaurant is not part of this order");
    boolean allowed = selected.stream().allMatch(item ->
      (item.getRestaurantStatus() == OrderStatus.PLACED && next == OrderStatus.ACCEPTED)
      || (item.getRestaurantStatus() == OrderStatus.ACCEPTED && next == OrderStatus.PREPARING)
      || (item.getRestaurantStatus() == OrderStatus.PREPARING && next == OrderStatus.READY));
    if (!allowed) throw new IllegalArgumentException("Restaurant orders must move from placed to accepted to preparing to ready");
    // Materialize legacy states before changing the overall status used as their fallback.
    items.forEach(item -> item.setRestaurantStatus(item.getRestaurantStatus()));
    selected.forEach(item -> item.setRestaurantStatus(next));
    status = items.stream().map(OrderItemEntity::getRestaurantStatus)
      .min(java.util.Comparator.comparingInt(Enum::ordinal)).orElse(status);
  }
}
