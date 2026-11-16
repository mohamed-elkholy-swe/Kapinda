package com.deliveryapp.order;

public record OrderItemResponse(
  String restaurantId,
  String restaurantName,
  String menuItemId,
  String itemName,
  int quantity,
  int unitPrice,
  int lineTotal,
  OrderStatus restaurantStatus
) {
  public OrderItemResponse(String restaurantId,String restaurantName,String menuItemId,String itemName,int quantity,int unitPrice,int lineTotal) {
    this(restaurantId,restaurantName,menuItemId,itemName,quantity,unitPrice,lineTotal,null);
  }
}
