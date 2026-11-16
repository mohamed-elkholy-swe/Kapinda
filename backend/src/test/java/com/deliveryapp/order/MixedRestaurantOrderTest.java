package com.deliveryapp.order;

import com.deliveryapp.account.AccountRepository;
import com.deliveryapp.notification.NotificationService;
import com.deliveryapp.restaurant.RestaurantService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MixedRestaurantOrderTest {
  private OrderEntity order(OrderStatus status) {
    var order = new OrderEntity(status,"buyer","phone","address","City",150,18,168,Instant.now());
    order.addItem(new OrderItemEntity("a","Kitchen A","a1","First meal",1,50,50));
    order.addItem(new OrderItemEntity("a","Kitchen A","a2","Second meal",1,50,50));
    order.addItem(new OrderItemEntity("b","Kitchen B","b1","Other meal",1,50,50));
    return order;
  }
  @Test void collectionWaitsForEveryKitchenAndNotificationsTrackOverallProgress() {
    var repository=mock(OrderRepository.class);
    var service=new OrderService(mock(RestaurantService.class),repository,mock(AccountRepository.class),mock(DeliveryAreaRepository.class));
    var notifications=mock(NotificationService.class); service.setNotifications(notifications);
    var order=order(OrderStatus.PLACED); when(repository.lockById(1L)).thenReturn(Optional.of(order));
    var delivery=new DeliveryService(repository,service);
    for (var next : new OrderStatus[]{OrderStatus.ACCEPTED,OrderStatus.PREPARING,OrderStatus.READY}) {
      service.updateRestaurantStatus(order,"a",next,"staff-a");
      assertEquals(next,order.getItems().get(0).getRestaurantStatus());
      assertEquals(next,order.getItems().get(1).getRestaurantStatus());
      assertEquals(OrderStatus.PLACED,order.getStatus());
      assertThrows(IllegalArgumentException.class,()->delivery.claim("courier",1L));
    }
    verifyNoInteractions(notifications);
    for (var next : new OrderStatus[]{OrderStatus.ACCEPTED,OrderStatus.PREPARING,OrderStatus.READY}) {
      service.updateRestaurantStatus(order,"b",next,"staff-b");
      assertEquals(next,order.getStatus());
    }
    verify(notifications,times(3)).record(order,"staff-b");
    delivery.claim("courier",1L); delivery.update("courier",1L,OrderStatus.OUT_FOR_DELIVERY);
    assertThrows(IllegalArgumentException.class,()->order.updateRestaurantStatus("a",OrderStatus.PREPARING));
    delivery.update("courier",1L,OrderStatus.DELIVERED);
    assertEquals(OrderStatus.DELIVERED,order.getStatus());
  }
  @Test void oldOrdersInheritKitchenStatusWithoutAdvancingOtherRestaurants() {
    var order=order(OrderStatus.ACCEPTED);
    order.updateRestaurantStatus("a",OrderStatus.PREPARING);
    assertEquals(OrderStatus.ACCEPTED,order.getStatus());
    assertEquals(OrderStatus.ACCEPTED,order.getItems().get(2).getRestaurantStatus());
    order.updateRestaurantStatus("b",OrderStatus.PREPARING);
    assertEquals(OrderStatus.PREPARING,order.getStatus());
    order.setStatus(OrderStatus.PLACED);
    assertTrue(order.getItems().stream().allMatch(item->item.getRestaurantStatus()==OrderStatus.PLACED));
    assertThrows(IllegalArgumentException.class,()->order.updateRestaurantStatus("a",OrderStatus.READY));
    assertThrows(IllegalArgumentException.class,()->order.updateRestaurantStatus("unknown",OrderStatus.ACCEPTED));
    order.setStatus(OrderStatus.CANCELLED);
    assertEquals(OrderStatus.PLACED,order.getItems().get(0).getRestaurantStatus());
    assertThrows(IllegalArgumentException.class,()->order.updateRestaurantStatus("a",OrderStatus.ACCEPTED));
  }
}
