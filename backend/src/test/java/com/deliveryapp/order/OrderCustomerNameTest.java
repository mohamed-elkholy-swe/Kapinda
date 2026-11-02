package com.deliveryapp.order;

import com.deliveryapp.account.*;
import com.deliveryapp.restaurant.RestaurantService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCustomerNameTest {
  @Test void responseIncludesNameFromTheOrderingAccount() {
    var accounts = mock(AccountRepository.class);
    var account = new AccountEntity("buyer", "buyer@example.test", AccountRole.CUSTOMER, AccountStatus.ACTIVE);
    account.updateProfile("  Ahmed Ali  ", "01012345678");
    when(accounts.findById("buyer")).thenReturn(Optional.of(account));
    var service = new OrderService(mock(RestaurantService.class), mock(OrderRepository.class), accounts);
    var order = new OrderEntity(OrderStatus.PLACED, "buyer", "phone", "address", "City", 50, 18, 68, Instant.now());
    assertEquals("Ahmed Ali", service.toResponse(order).customerName());
    verify(accounts).findById("buyer");
  }

  @Test void missingAccountOrProfileNameReturnsEmptyName() {
    var accounts = mock(AccountRepository.class);
    var service = new OrderService(mock(RestaurantService.class), mock(OrderRepository.class), accounts);
    var order = new OrderEntity(OrderStatus.PLACED, "buyer", "phone", "address", "City", 50, 18, 68, Instant.now());
    when(accounts.findById("buyer")).thenReturn(Optional.empty());
    assertEquals("", service.toResponse(order).customerName());
    when(accounts.findById("buyer")).thenReturn(Optional.of(new AccountEntity("buyer", "buyer@example.test", AccountRole.CUSTOMER, AccountStatus.ACTIVE)));
    assertEquals("", service.toResponse(order).customerName());
    var legacy = new OrderEntity(OrderStatus.PLACED, null, "phone", "address", "City", 50, 18, 68, Instant.now());
    assertEquals("", service.toResponse(legacy).customerName());
  }
}
