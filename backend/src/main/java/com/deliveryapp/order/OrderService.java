package com.deliveryapp.order;

import com.deliveryapp.restaurant.MenuItemEntity;
import com.deliveryapp.restaurant.RestaurantEntity;
import com.deliveryapp.restaurant.RestaurantService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
  private com.deliveryapp.notification.NotificationService notifications;
  @org.springframework.beans.factory.annotation.Autowired
  public void setNotifications(com.deliveryapp.notification.NotificationService notifications) { this.notifications=notifications; }
  private void notifyOrder(OrderEntity order) { if(notifications!=null) notifications.record(order); }
  private static final int DELIVERY_FEE = 18;

  private final RestaurantService restaurantService;
  private final OrderRepository orderRepository;

  public OrderService(RestaurantService restaurantService, OrderRepository orderRepository) {
    this.restaurantService = restaurantService;
    this.orderRepository = orderRepository;
  }

  @Transactional
  public OrderResponse create(String customerUid, CreateOrderRequest request) {
    List<OrderItemEntity> orderItems = new ArrayList<>();

    for (CreateOrderItemRequest requestItem : request.items()) {
      RestaurantEntity restaurant = restaurantService.findEntityById(requestItem.restaurantId());
      MenuItemEntity menuItem = restaurantService.findMenuItemEntity(requestItem.restaurantId(), requestItem.menuItemId());
      int lineTotal = menuItem.getPrice() * requestItem.quantity();

      orderItems.add(new OrderItemEntity(
        restaurant.getId(),
        restaurant.getName(),
        menuItem.getId(),
        menuItem.getName(),
        requestItem.quantity(),
        menuItem.getPrice(),
        lineTotal
      ));
    }

    int subtotal = orderItems.stream().mapToInt(OrderItemEntity::getLineTotal).sum();
    OrderEntity order = new OrderEntity(
      OrderStatus.PLACED,
      customerUid,
      request.customerPhone(),
      request.customerAddress(),
      request.area(),
      subtotal,
      DELIVERY_FEE,
      subtotal + DELIVERY_FEE,
      Instant.now()
    );

    orderItems.forEach(order::addItem);
    OrderEntity saved=orderRepository.save(order);
    notifyOrder(saved);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public OrderResponse findByIdForCustomer(String customerUid, String id) {
    Long numericId = parseId(id);
    OrderEntity order = orderRepository.findById(numericId).orElseThrow(() -> new OrderNotFoundException(id));
    if (!customerUid.equals(order.getCustomerUid())) throw new OrderNotFoundException(id);
    return toResponse(order);
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> findAll() {
    return orderRepository.findAll().stream()
      .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
      .map(this::toResponse)
      .toList();
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> findAllForCustomer(String customerUid) {
    if (customerUid == null || customerUid.isBlank()) throw new SecurityException("A signed-in account is required");
    return orderRepository.findAllByCustomerUidOrderByCreatedAtDesc(customerUid).stream()
      .filter(order -> customerUid.equals(order.getCustomerUid()))
      .map(this::toResponse).toList();
  }

  @Transactional
  public OrderResponse updateStatus(String id, OrderStatus status) {
    OrderEntity order = orderRepository.findById(parseId(id)).orElseThrow(() -> new OrderNotFoundException(id));
    order.setStatus(status);
    notifyOrder(order);
    return toResponse(order);
  }

  OrderResponse toResponse(OrderEntity order) {
    return new OrderResponse(
      String.valueOf(order.getId()),
      order.getStatus(),
      order.getCustomerPhone(),
      order.getCustomerAddress(),
      order.getArea(),
      order.getItems().stream()
        .map(item -> new OrderItemResponse(
          item.getRestaurantId(),
          item.getRestaurantName(),
          item.getMenuItemId(),
          item.getItemName(),
          item.getQuantity(),
          item.getUnitPrice(),
          item.getLineTotal()
        ))
        .toList(),
      order.getSubtotal(),
      order.getDeliveryFee(),
      order.getTotal(),
      order.getCreatedAt()
    );
  }

  private Long parseId(String id) {
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException exception) {
      throw new OrderNotFoundException(id);
    }
  }
}
