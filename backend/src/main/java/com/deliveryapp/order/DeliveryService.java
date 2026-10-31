package com.deliveryapp.order;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {
  private final OrderRepository repository;
  private final OrderService orders;
  public DeliveryService(OrderRepository repository, OrderService orders) { this.repository = repository; this.orders = orders; }
  public record Task(boolean assignedToMe, boolean assignedToOther, boolean canClaim, boolean canUpdate, OrderResponse order) {}
  @Transactional(readOnly = true)
  public List<Task> list(String uid) {
    return list(uid, false);
  }
  @Transactional(readOnly = true)
  public List<Task> list(String uid, boolean supervisor) {
    var source = supervisor ? repository.findAll().stream()
      .filter(order -> order.getDeliveryPersonUid() != null || java.util.Set.of(OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED).contains(order.getStatus()))
      .sorted(java.util.Comparator.comparing(OrderEntity::getCreatedAt).reversed()).toList() : repository.findDeliveryTasks(uid);
    return source.stream().map(order -> {
      boolean mine = uid.equals(order.getDeliveryPersonUid());
      boolean assigned = order.getDeliveryPersonUid() != null;
      OrderResponse full = orders.toResponse(order);
      OrderResponse visible = mine || supervisor ? full : new OrderResponse(full.id(), full.status(), "", "", full.area(), full.items(), full.subtotal(), full.deliveryFee(), full.total(), full.createdAt());
      return new Task(mine, assigned && !mine, !assigned && order.getStatus() == OrderStatus.READY,
        assigned && (mine || supervisor) && (order.getStatus() == OrderStatus.READY || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY), visible);
    }).toList();
  }
  @Transactional
  public void claim(String uid, Long id) {
    OrderEntity order = repository.lockById(id).orElseThrow(() -> new OrderNotFoundException(String.valueOf(id)));
    if (order.getStatus() != OrderStatus.READY || (order.getDeliveryPersonUid() != null && !uid.equals(order.getDeliveryPersonUid())))
      throw new IllegalArgumentException("This order is no longer available for collection");
    order.assignDeliveryPerson(uid);
  }
  @Transactional
  public void update(String uid, Long id, OrderStatus next) {
    update(uid, id, next, false);
  }
  @Transactional
  public void update(String uid, Long id, OrderStatus next, boolean supervisor) {
    OrderEntity order = repository.lockById(id).orElseThrow(() -> new OrderNotFoundException(String.valueOf(id)));
    if (!supervisor && !uid.equals(order.getDeliveryPersonUid())) throw new SecurityException("This delivery is not assigned to you");
    if (order.getDeliveryPersonUid() == null) throw new IllegalArgumentException("Claim this order before starting delivery");
    boolean allowed = (order.getStatus() == OrderStatus.READY && next == OrderStatus.OUT_FOR_DELIVERY)
      || (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY && next == OrderStatus.DELIVERED);
    if (!allowed) throw new IllegalArgumentException("Delivery status can only move from ready to out for delivery to delivered");
    order.setStatus(next);
  }
}
