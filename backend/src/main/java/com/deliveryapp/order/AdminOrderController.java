package com.deliveryapp.order;

import com.deliveryapp.account.AccountRole;
import com.deliveryapp.account.AccountService;
import com.deliveryapp.account.AuthenticatedUser;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
  private final OrderService orderService;
  private final AccountService accountService;

  public AdminOrderController(OrderService orderService, AccountService accountService) {
    this.orderService = orderService;
    this.accountService = accountService;
  }

  @GetMapping
  public List<OrderResponse> findAll(@RequestAttribute AuthenticatedUser authenticatedUser) {
    accountService.requireRole(authenticatedUser, AccountRole.ADMIN);
    return orderService.findAll();
  }

  @PutMapping("/{id}/status")
  public OrderResponse updateStatus(
    @RequestAttribute AuthenticatedUser authenticatedUser,
    @PathVariable String id,
    @RequestParam OrderStatus status
  ) {
    accountService.requireRole(authenticatedUser, AccountRole.ADMIN);
    return orderService.updateStatus(id, status);
  }
}
