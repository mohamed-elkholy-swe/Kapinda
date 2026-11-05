package com.deliveryapp.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deliveryapp.account.*;
import com.deliveryapp.order.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GuestCheckoutTest {
  private final FirebaseAuthenticationFilter filter = new FirebaseAuthenticationFilter("", "");

  @Test
  void guestCanSubmitAValidatedOrderWithoutFirebase() throws Exception {
    OrderService orders = mock(OrderService.class);
    AccountService accounts = mock(AccountService.class);
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new OrderController(orders, accounts))
      .addFilters(filter).build();
    mvc.perform(post("/api/orders").contentType("application/json").content("""
      {"customerPhone":"01012345678","customerAddress":"Street 12, floor 2","area":"Central",
       "items":[{"restaurantId":"1","menuItemId":"2","quantity":1}]}
      """)).andExpect(status().isCreated());
    verify(orders).create(isNull(), any(CreateOrderRequest.class));
    verifyNoInteractions(accounts);
    mvc.perform(post("/api/orders").contentType("application/json").content("{}"))
      .andExpect(status().isBadRequest());
  }

  @Test
  void historyDetailsStaffAndSuppliedCredentialsStillRequireAuthentication() throws Exception {
    for (String path : new String[]{"/api/orders", "/api/orders/1", "/api/admin/orders"}) {
      var response = new MockHttpServletResponse();
      filter.doFilter(new MockHttpServletRequest("GET", path), response,
        (request, result) -> fail("Protected request passed authentication"));
      assertEquals(503, response.getStatus());
    }
    var request = new MockHttpServletRequest("POST", "/api/orders");
    request.addHeader("Authorization", "Bearer invalid");
    var response = new MockHttpServletResponse();
    filter.doFilter(request, response, (req, res) -> fail("Credentials must be verified"));
    assertEquals(503, response.getStatus());
  }
}
