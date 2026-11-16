package com.deliveryapp.order;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.deliveryapp.account.*;
import org.junit.jupiter.api.Test;
class LaunchOrdersTest {
  private final OrderService orders = mock(OrderService.class);
  private final AccountService accounts = mock(AccountService.class);
  private final AuthenticatedUser user = new AuthenticatedUser("tester", "tester@example.com");
  private final OrderController controller = new OrderController(orders, accounts);
  @Test void guestsCanOrderWithoutALaunchDateRestriction() {
    controller.create(null, null);
    verify(orders).create(null, null);
    verifyNoInteractions(accounts);
  }
  @Test void activeAccountsCanOrder() {
    for (var role : AccountRole.values()) {
      when(accounts.requireActive(user)).thenReturn(new AccountEntity(user.uid(), user.email(), role, AccountStatus.ACTIVE));
      controller.create(user, null);
    }
    verify(orders, times(AccountRole.values().length)).create(user.uid(), null);
  }
  @Test void inactiveAccountsStillCannotOrder() {
    when(accounts.requireActive(user)).thenThrow(new SecurityException("Access denied"));
    assertThrows(SecurityException.class, () -> controller.create(user, null));
    verifyNoInteractions(orders);
  }
}
