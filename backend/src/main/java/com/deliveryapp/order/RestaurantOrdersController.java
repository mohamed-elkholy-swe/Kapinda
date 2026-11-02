package com.deliveryapp.order;
import com.deliveryapp.account.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/restaurant-workspace/orders")
public class RestaurantOrdersController {
  private final AccountService accounts;
  private final OrderRepository repository;
  private final OrderService orders;
  public RestaurantOrdersController(AccountService accounts, OrderRepository repository, OrderService orders) { this.accounts=accounts; this.repository=repository; this.orders=orders; }
  @GetMapping @Transactional(readOnly=true)
  public List<OrderResponse> list(@RequestAttribute AuthenticatedUser authenticatedUser) {
    var account=accounts.requireRole(authenticatedUser, AccountRole.RESTAURANT);
    if(account.getRole().isAdministrator()) return orders.findAll();
    var ids=account.getRestaurantIds();
    if(ids.isEmpty()) return List.of();
    return repository.findAssignedRestaurantOrders(ids).stream().map(order -> {
      var items=order.getItems().stream().filter(item -> ids.contains(item.getRestaurantId())).toList();
      int subtotal=items.stream().mapToInt(OrderItemEntity::getLineTotal).sum();
      return new OrderResponse(String.valueOf(order.getId()),order.getStatus(),"","",order.getArea(),items.stream().map(i -> new OrderItemResponse(i.getRestaurantId(),i.getRestaurantName(),i.getMenuItemId(),i.getItemName(),i.getQuantity(),i.getUnitPrice(),i.getLineTotal())).toList(),subtotal,0,subtotal,order.getCreatedAt(),!order.getItems().isEmpty() && order.getItems().stream().allMatch(i->ids.contains(i.getRestaurantId())),orders.customerName(order));
    }).toList();
  }

  @PutMapping("/{id}/status")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  @Transactional
  public void updateStatus(@RequestAttribute AuthenticatedUser authenticatedUser,@PathVariable Long id,@RequestParam OrderStatus status) {
    var account=accounts.requireRole(authenticatedUser,AccountRole.RESTAURANT);
    var order=repository.lockById(id).orElseThrow(()->new OrderNotFoundException(String.valueOf(id)));
    if(!account.getRole().isAdministrator() && (order.getItems().isEmpty() || order.getItems().stream().anyMatch(i->!account.getRestaurantIds().contains(i.getRestaurantId())))) throw new SecurityException("Only assigned restaurants can update this order. Mixed-restaurant orders require an administrator.");
    boolean allowed=(order.getStatus()==OrderStatus.PLACED && status==OrderStatus.ACCEPTED)
      ||(order.getStatus()==OrderStatus.ACCEPTED && status==OrderStatus.PREPARING)
      ||(order.getStatus()==OrderStatus.PREPARING && status==OrderStatus.READY);
    if(!allowed) throw new IllegalArgumentException("Restaurant orders must move from placed to accepted to preparing to ready.");
    orders.updateStatus(String.valueOf(id),status);
  }
}
