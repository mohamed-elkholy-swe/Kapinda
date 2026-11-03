package com.deliveryapp.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deliveryapp.account.*;
import com.deliveryapp.restaurant.RestaurantService;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderHistoryTest {
  @Test
  void historyUsesAuthenticatedIdentityAndRequiresActiveAccount() {
    OrderService orders = mock(OrderService.class);
    AccountService accounts = mock(AccountService.class);
    AuthenticatedUser user = new AuthenticatedUser("customer-a", "a@example.test");
    when(orders.findAllForCustomer("customer-a")).thenReturn(List.of());
    assertTrue(new OrderController(orders, accounts).history(user).isEmpty());
    verify(accounts).requireActive(user);
    verify(orders).findAllForCustomer("customer-a");
    verifyNoMoreInteractions(orders);
  }

  @Test
  void historyNeverReadsUnfilteredOrders() {
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findAllByCustomerUidOrderByCreatedAtDesc("customer-a")).thenReturn(List.of());
    OrderService service = new OrderService(mock(RestaurantService.class), repository, mock(com.deliveryapp.account.AccountRepository.class), mock(DeliveryAreaRepository.class));
    assertTrue(service.findAllForCustomer("customer-a").isEmpty());
    verify(repository).findAllByCustomerUidOrderByCreatedAtDesc("customer-a");
    verify(repository, never()).findAll();
  }

  @Test
  void deniedAccountCannotReadHistory() {
    OrderService orders = mock(OrderService.class);
    AccountService accounts = mock(AccountService.class);
    AuthenticatedUser user = new AuthenticatedUser("admin", "admin@example.test");
    doThrow(new SecurityException("Access denied")).when(accounts).requireActive(user);
    assertThrows(SecurityException.class, () -> new OrderController(orders, accounts).history(user));
    verifyNoInteractions(orders);
  }
}
