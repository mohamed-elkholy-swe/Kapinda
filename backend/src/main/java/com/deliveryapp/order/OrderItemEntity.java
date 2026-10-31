package com.deliveryapp.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @Column(name = "restaurant_id", nullable = false)
  private String restaurantId;

  @Column(name = "restaurant_name", nullable = false)
  private String restaurantName;

  @Column(name = "menu_item_id", nullable = false)
  private String menuItemId;

  @Column(name = "item_name", nullable = false)
  private String itemName;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "unit_price", nullable = false)
  private int unitPrice;

  @Column(name = "line_total", nullable = false)
  private int lineTotal;

  protected OrderItemEntity() {}

  public OrderItemEntity(
    String restaurantId,
    String restaurantName,
    String menuItemId,
    String itemName,
    int quantity,
    int unitPrice,
    int lineTotal
  ) {
    this.restaurantId = restaurantId;
    this.restaurantName = restaurantName;
    this.menuItemId = menuItemId;
    this.itemName = itemName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.lineTotal = lineTotal;
  }

  public void setOrder(OrderEntity order) {
    this.order = order;
  }

  public String getRestaurantId() {
    return restaurantId;
  }

  public String getRestaurantName() {
    return restaurantName;
  }

  public String getMenuItemId() {
    return menuItemId;
  }

  public String getItemName() {
    return itemName;
  }

  public int getQuantity() {
    return quantity;
  }

  public int getUnitPrice() {
    return unitPrice;
  }

  public int getLineTotal() {
    return lineTotal;
  }
}
