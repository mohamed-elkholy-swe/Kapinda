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
  private void notifyOrder(OrderEntity order, String actorUid) { if(notifications!=null) notifications.record(order, actorUid); }

  private final RestaurantService restaurantService;
  private final OrderRepository orderRepository;
  private final com.deliveryapp.account.AccountRepository accounts;

  private final DeliveryAreaRepository deliveryAreas;

  public OrderService(RestaurantService restaurantService, OrderRepository orderRepository, com.deliveryapp.account.AccountRepository accounts, DeliveryAreaRepository deliveryAreas) {
    this.restaurantService = restaurantService;
    this.orderRepository = orderRepository;
    this.accounts = accounts;
    this.deliveryAreas = deliveryAreas;
  }

  @Transactional
  public OrderResponse create(String customerUid, CreateOrderRequest request) {
    var area = deliveryAreas.findByName(request.area().trim())
      .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Select a supported delivery area"));
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
      area.getName(),
      subtotal,
      area.getFee(),
      subtotal + area.getFee(),
      Instant.now()
    );

    orderItems.forEach(order::addItem);
    OrderEntity saved=orderRepository.save(order);
    notifyOrder(saved, customerUid);
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
  public OrderResponse updateStatus(String id, OrderStatus status, String actorUid) {
    OrderEntity order = orderRepository.findById(parseId(id)).orElseThrow(() -> new OrderNotFoundException(id));
    if (order.getStatus() == status) return toResponse(order);
    order.setStatus(status);
    notifyOrder(order, actorUid);
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
      order.getCreatedAt(),
      false,
      customerName(order)
    );
  }

  String customerName(OrderEntity order) {
    if (order.getCustomerUid() == null || order.getCustomerUid().isBlank()) return "";
    return accounts.findById(order.getCustomerUid())
      .map(com.deliveryapp.account.AccountEntity::getDisplayName)
      .map(String::trim).orElse("");
  }

  private Long parseId(String id) {
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException exception) {
      throw new OrderNotFoundException(id);
    }
  }
}
