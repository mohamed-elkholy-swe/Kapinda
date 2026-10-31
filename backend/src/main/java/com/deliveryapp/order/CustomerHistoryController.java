package com.deliveryapp.order;

import com.deliveryapp.account.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account/me/orders")
public class CustomerHistoryController {
  private final AccountService accounts;
  private final OrderService orders;
  public CustomerHistoryController(AccountService accounts, OrderService orders) { this.accounts = accounts; this.orders = orders; }
  public record History(String accountUid, List<OrderResponse> orders) {}
  @GetMapping
  public History history(@RequestAttribute AuthenticatedUser authenticatedUser, jakarta.servlet.http.HttpServletResponse response) {
    accounts.requireActive(authenticatedUser);
    response.setHeader("Cache-Control", "no-store, private");
    return new History(authenticatedUser.uid(), orders.findAllForCustomer(authenticatedUser.uid()));
  }
}
