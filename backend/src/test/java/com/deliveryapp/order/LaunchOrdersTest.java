package com.deliveryapp.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.deliveryapp.account.*;
import java.time.*;
import org.junit.jupiter.api.Test;

class LaunchOrdersTest {
  @Test void directGuestApiRequestsReceiveTheLaunchMessageAndCreateNothing() throws Exception {
    var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
      .standaloneSetup(controller(OrderController.LAUNCH_AT.minusSeconds(1)))
      .setControllerAdvice(new com.deliveryapp.shared.ApiExceptionHandler()).build();
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/orders")
      .contentType("application/json").content("""
        {"customerPhone":"01012345678","customerAddress":"Street 12, floor 2","area":"Central",
         "items":[{"restaurantId":"1","menuItemId":"2","quantity":1}]}
        """))
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden())
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("4:00 PM")));
    verifyNoInteractions(orders);
  }

  private final OrderService orders = mock(OrderService.class);
  private final AccountService accounts = mock(AccountService.class);
  private final AuthenticatedUser user = new AuthenticatedUser("tester", "tester@example.com");

  private OrderController controller(Instant now) {
    return new OrderController(orders, accounts, Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test void guestsAndNonAdministratorsCannotCreateOrdersBeforeLaunch() {
    var controller = controller(OrderController.LAUNCH_AT.minusSeconds(1));
    assertTrue(assertThrows(SecurityException.class, () -> controller.create(null, null)).getMessage().contains("4:00 PM"));
    for (var role : new AccountRole[]{AccountRole.CUSTOMER, AccountRole.RESTAURANT, AccountRole.DELIVERY}) {
      when(accounts.requireActive(user)).thenReturn(new AccountEntity(user.uid(), user.email(), role, AccountStatus.ACTIVE));
      assertThrows(SecurityException.class, () -> controller.create(user, null));
    }
    verifyNoInteractions(orders);
  }

  @Test void activeAdministratorsCanTestBeforeLaunch() {
    var controller = controller(OrderController.LAUNCH_AT.minusSeconds(1));
    for (var role : new AccountRole[]{AccountRole.ADMIN, AccountRole.SUB_ADMIN}) {
      when(accounts.requireActive(user)).thenReturn(new AccountEntity(user.uid(), user.email(), role, AccountStatus.ACTIVE));
      controller.create(user, null);
    }
    verify(orders, times(2)).create(user.uid(), null);
  }

  @Test void inactiveAccountsCannotUseTheAdminException() {
    when(accounts.requireActive(user)).thenThrow(new SecurityException("Access denied"));
    assertThrows(SecurityException.class, () -> controller(OrderController.LAUNCH_AT.minusSeconds(1)).create(user, null));
    verifyNoInteractions(orders);
  }

  @Test void publicOrdersOpenAtExactlyFourPmCairoTime() {
    assertEquals(Instant.parse("2026-11-12T14:00:00Z"), OrderController.LAUNCH_AT);
    var controller = controller(OrderController.LAUNCH_AT);
    controller.create(null, null);
    when(accounts.requireActive(user)).thenReturn(new AccountEntity(user.uid(), user.email(), AccountRole.CUSTOMER, AccountStatus.ACTIVE));
    controller.create(user, null);
    verify(orders).create(null, null);
    verify(orders).create(user.uid(), null);
  }
}
