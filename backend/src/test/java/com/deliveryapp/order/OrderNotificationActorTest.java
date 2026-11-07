package com.deliveryapp.order;

import com.deliveryapp.account.*;
import com.deliveryapp.notification.NotificationService;
import com.deliveryapp.restaurant.RestaurantService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class OrderNotificationActorTest {
  final OrderRepository repository=mock(OrderRepository.class);
  final NotificationService notifications=mock(NotificationService.class);
  final OrderService orders=new OrderService(mock(RestaurantService.class),repository,mock(AccountRepository.class),mock(DeliveryAreaRepository.class));
  OrderEntity order(OrderStatus status) {
    var order=new OrderEntity(status,"buyer","phone","address","City",50,18,68,Instant.now());
    when(repository.findById(1L)).thenReturn(Optional.of(order));when(repository.lockById(1L)).thenReturn(Optional.of(order));
    orders.setNotifications(notifications);return order;
  }
  @Test void administratorIdentityReachesNotificationService() {
    var order=order(OrderStatus.PLACED);var actor=new AuthenticatedUser("admin","admin@example.test");
    new AdminOrderController(orders,mock(AccountService.class)).updateStatus(actor,"1",OrderStatus.ACCEPTED);
    verify(notifications).record(order,"admin");
  }
  @Test void repeatingTheCurrentStatusDoesNotCreateNotifications() {
    order(OrderStatus.ACCEPTED);orders.updateStatus("1",OrderStatus.ACCEPTED,"admin");verifyNoInteractions(notifications);
  }
  @Test void restaurantIdentityReachesNotificationService() {
    var order=order(OrderStatus.PLACED);order.addItem(new OrderItemEntity("kitchen","Kitchen","dish","Dish",1,50,50));
    var actor=new AuthenticatedUser("restaurant","restaurant@example.test");var accounts=mock(AccountService.class);
    var account=new AccountEntity(actor.uid(),actor.email(),AccountRole.RESTAURANT,AccountStatus.ACTIVE);account.assignAccess(AccountRole.RESTAURANT,java.util.Set.of("kitchen"));
    when(accounts.requireRole(actor,AccountRole.RESTAURANT)).thenReturn(account);
    new RestaurantOrdersController(accounts,repository,orders).updateStatus(actor,1L,OrderStatus.ACCEPTED);
    verify(notifications).record(order,"restaurant");
  }
  @Test void courierAndSupervisorIdentityIsUsedRatherThanAssignedCourier() {
    for (boolean supervisor : new boolean[]{false,true}) {
      var order=order(OrderStatus.READY);order.assignDeliveryPerson("courier");
      var delivery=new DeliveryService(repository,orders);delivery.setNotifications(notifications);
      var actor=supervisor?"admin":"courier";delivery.update(actor,1L,OrderStatus.OUT_FOR_DELIVERY,supervisor);
      verify(notifications).record(order,actor);
    }
  }
}
