package com.deliveryapp.order;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
  String id,
  OrderStatus status,
  String customerPhone,
  String customerAddress,
  String area,
  List<OrderItemResponse> items,
  int subtotal,
  int deliveryFee,
  int total,
  Instant createdAt,
  boolean restaurantCanUpdate,
  String customerName
) {
  public OrderResponse(String id,OrderStatus status,String customerPhone,String customerAddress,String area,List<OrderItemResponse> items,int subtotal,int deliveryFee,int total,Instant createdAt) {
    this(id,status,customerPhone,customerAddress,area,items,subtotal,deliveryFee,total,createdAt,false, "");
  }
  public OrderResponse(String id,OrderStatus status,String customerPhone,String customerAddress,String area,List<OrderItemResponse> items,int subtotal,int deliveryFee,int total,Instant createdAt,boolean restaurantCanUpdate) {
    this(id,status,customerPhone,customerAddress,area,items,subtotal,deliveryFee,total,createdAt,restaurantCanUpdate, "");
  }
}
