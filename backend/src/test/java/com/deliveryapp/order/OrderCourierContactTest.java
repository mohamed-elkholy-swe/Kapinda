package com.deliveryapp.order;

import com.deliveryapp.account.*;
import com.deliveryapp.restaurant.RestaurantService;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCourierContactTest {
  @Test void responseIncludesOnlyTheAssignedCouriersContactAndHandlesMissingProfiles() {
    var accounts=mock(AccountRepository.class);
    var orders=new OrderService(mock(RestaurantService.class),mock(OrderRepository.class),accounts,mock(DeliveryAreaRepository.class));
    var order=new OrderEntity(OrderStatus.READY,null,"phone","address","City",50,18,68,Instant.now());
    assertFalse(orders.toResponse(order).deliveryAssigned());
    assertEquals("",orders.toResponse(order).deliveryPersonPhone());
    verifyNoInteractions(accounts);
    order.assignDeliveryPerson("courier");
    var courier=new AccountEntity("courier","courier@example.test",AccountRole.DELIVERY,AccountStatus.ACTIVE);
    courier.updateProfile(" Mohamed Hassan ","01012345678");
    when(accounts.findById("courier")).thenReturn(Optional.of(courier));
    assertTrue(orders.toResponse(order).deliveryAssigned());
    assertEquals("Mohamed Hassan",orders.toResponse(order).deliveryPersonName());
    assertEquals("01012345678",orders.toResponse(order).deliveryPersonPhone());
    when(accounts.findById("courier")).thenReturn(Optional.empty());
    assertTrue(orders.toResponse(order).deliveryAssigned());
    assertEquals("",orders.toResponse(order).deliveryPersonName());
    assertEquals("",orders.toResponse(order).deliveryPersonPhone());
  }
}
