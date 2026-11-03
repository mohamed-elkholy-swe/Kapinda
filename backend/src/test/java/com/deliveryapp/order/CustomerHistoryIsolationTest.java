package com.deliveryapp.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.deliveryapp.account.*;
import com.deliveryapp.restaurant.RestaurantService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class CustomerHistoryIsolationTest {
  @Test void brandNewCustomerSeesZeroEvenWhenOtherCustomersHaveOrders() {
    var repository = mock(OrderRepository.class);
    var foreign = new OrderEntity(OrderStatus.PLACED, "existing-customer", "01012345678", "Other address", "Area", 100, 18, 118, Instant.now());
    // The service also checks ownership if a repository ever returns an unrelated record.
    when(repository.findAllByCustomerUidOrderByCreatedAtDesc("new-customer")).thenReturn(List.of(foreign));
    var service = new OrderService(mock(RestaurantService.class), repository, mock(com.deliveryapp.account.AccountRepository.class), mock(DeliveryAreaRepository.class));
    assertTrue(service.findAllForCustomer("new-customer").isEmpty());
    verify(repository, never()).findAll();
  }
  @Test void historyResponseIsBoundToVerifiedIdentityAndCannotBeCached() {
    var accounts = mock(AccountService.class); var orders = mock(OrderService.class);
    var response = mock(HttpServletResponse.class);
    var user = new AuthenticatedUser("new-customer", "new@example.test");
    when(orders.findAllForCustomer("new-customer")).thenReturn(List.of());
    var result = new CustomerHistoryController(accounts, orders).history(user, response);
    assertEquals("new-customer", result.accountUid()); assertTrue(result.orders().isEmpty());
    verify(accounts).requireActive(user);
    verify(response).setHeader("Cache-Control", "no-store, private");
  }
  @Test void blankIdentityCannotFallBackToThePlatformOrderList() {
    var repository = mock(OrderRepository.class);
    var service = new OrderService(mock(RestaurantService.class), repository, mock(com.deliveryapp.account.AccountRepository.class), mock(DeliveryAreaRepository.class));
    assertThrows(SecurityException.class, () -> service.findAllForCustomer(""));
    verifyNoInteractions(repository);
  }
}
