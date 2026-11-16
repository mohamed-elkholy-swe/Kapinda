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
      var full=orders.toResponse(order);
      int subtotal=items.stream().mapToInt(OrderItemEntity::getLineTotal).sum();
      return new OrderResponse(String.valueOf(order.getId()),order.getStatus(),"","",order.getArea(),items.stream().map(i -> new OrderItemResponse(i.getRestaurantId(),i.getRestaurantName(),i.getMenuItemId(),i.getItemName(),i.getQuantity(),i.getUnitPrice(),i.getLineTotal(),i.getRestaurantStatus())).toList(),subtotal,0,subtotal,order.getCreatedAt(),!items.isEmpty(),full.customerName(),full.deliveryAssigned(),full.deliveryPersonName(),full.deliveryPersonPhone());
    }).toList();
  }

  @PutMapping("/{id}/status")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  @Transactional
  public void updateStatus(@RequestAttribute AuthenticatedUser authenticatedUser,@PathVariable Long id,@RequestParam OrderStatus status,@RequestParam(required=false) String restaurantId) {
    var account=accounts.requireRole(authenticatedUser,AccountRole.RESTAURANT);
    var order=repository.lockById(id).orElseThrow(()->new OrderNotFoundException(String.valueOf(id)));
    var candidates=order.getItems().stream().map(OrderItemEntity::getRestaurantId).distinct()
      .filter(restaurant -> account.getRole().isAdministrator() || account.getRestaurantIds().contains(restaurant)).toList();
    if(candidates.isEmpty()) throw new SecurityException("Only assigned restaurants can update this order");
    String selectedRestaurant=restaurantId;
    if(selectedRestaurant==null || selectedRestaurant.isBlank()) {
      if(candidates.size()!=1) throw new IllegalArgumentException("Choose the restaurant to update");
      selectedRestaurant=candidates.get(0);
    }
    if(!candidates.contains(selectedRestaurant)) throw new SecurityException("Only assigned restaurants can update this order");
    orders.updateRestaurantStatus(order,selectedRestaurant,status,authenticatedUser.uid());
  }
}
