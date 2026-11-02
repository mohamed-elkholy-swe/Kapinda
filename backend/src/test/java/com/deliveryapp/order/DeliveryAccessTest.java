package com.deliveryapp.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.deliveryapp.account.*;
import com.deliveryapp.restaurant.RestaurantService;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class DeliveryAccessTest {
  private final OrderRepository repository = mock(OrderRepository.class);
  private final DeliveryService service = new DeliveryService(repository, new OrderService(mock(RestaurantService.class), repository, mock(com.deliveryapp.account.AccountRepository.class)));
  private OrderEntity order() {
    var order = new OrderEntity(OrderStatus.READY, "customer", "01012345678", "Private address", "City area", 100, 18, 118, Instant.now());
    when(repository.lockById(1L)).thenReturn(Optional.of(order));
    return order;
  }
  @Test void unclaimedOrderHidesContactDetailsUntilClaimed() {
    var order = order(); when(repository.findDeliveryTasks("courier-a")).thenReturn(List.of(order));
    var before = service.list("courier-a").get(0);
    assertFalse(before.assignedToMe()); assertEquals("", before.order().customerPhone()); assertEquals("", before.order().customerAddress());
    service.claim("courier-a", 1L);
    var after = service.list("courier-a").get(0);
    assertTrue(after.assignedToMe()); assertEquals("01012345678", after.order().customerPhone()); assertEquals("Private address", after.order().customerAddress());
  }
  @Test void secondCourierCannotClaimOrUpdateSomeoneElsesDelivery() {
    var order = order(); service.claim("courier-a", 1L);
    assertThrows(IllegalArgumentException.class, () -> service.claim("courier-b", 1L));
    assertThrows(SecurityException.class, () -> service.update("courier-b", 1L, OrderStatus.OUT_FOR_DELIVERY));
    assertEquals("courier-a", order.getDeliveryPersonUid()); assertEquals(OrderStatus.READY, order.getStatus());
  }
  @Test void courierCannotSkipOrReverseDeliverySteps() {
    var order = order(); service.claim("courier-a", 1L);
    assertThrows(IllegalArgumentException.class, () -> service.update("courier-a", 1L, OrderStatus.DELIVERED));
    service.update("courier-a", 1L, OrderStatus.OUT_FOR_DELIVERY);
    assertThrows(IllegalArgumentException.class, () -> service.update("courier-a", 1L, OrderStatus.READY));
    service.update("courier-a", 1L, OrderStatus.DELIVERED);
    assertEquals(OrderStatus.DELIVERED, order.getStatus());
  }
  @Test void ordinaryCustomerCannotReadOrClaimDeliveryQueue() {
    var accounts = mock(AccountService.class); var delivery = mock(DeliveryService.class);
    var user = new AuthenticatedUser("customer", "customer@example.test");
    doThrow(new SecurityException("Access denied")).when(accounts).requireRole(user, AccountRole.DELIVERY);
    var controller = new DeliveryController(accounts, delivery);
    assertThrows(SecurityException.class, () -> controller.list(user));
    assertThrows(SecurityException.class, () -> controller.claim(user, 1L));
    verifyNoInteractions(delivery);
  }
  @Test void supervisorsSeeAndUpdateAnotherCouriersDeliveryWithoutTakingOwnership() {
    var order = order(); order.assignDeliveryPerson("courier-a");
    when(repository.findAll()).thenReturn(List.of(order));
    var task = service.list("administrator", true).get(0);
    assertFalse(task.assignedToMe()); assertTrue(task.assignedToOther());
    assertTrue(task.canUpdate()); assertFalse(task.canClaim());
    assertEquals("01012345678", task.order().customerPhone());
    service.update("administrator", 1L, OrderStatus.OUT_FOR_DELIVERY, true);
    assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());
    assertEquals("courier-a", order.getDeliveryPersonUid());
  }
  @Test void controllerDerivesSupervisorAccessFromBothAdministratorRoles() {
    var accountsRepository = mock(AccountRepository.class);
    var accounts = new AccountService(accountsRepository, "");
    var delivery = mock(DeliveryService.class);
    var actor = new AuthenticatedUser("staff", "staff@example.test");
    for (var role : List.of(AccountRole.ADMIN, AccountRole.SUB_ADMIN)) {
      when(accountsRepository.findById("staff")).thenReturn(Optional.of(new AccountEntity("staff", "staff@example.test", role, AccountStatus.ACTIVE)));
      new DeliveryController(accounts, delivery).list(actor);
    }
    verify(delivery, times(2)).list("staff", true);
  }
}
