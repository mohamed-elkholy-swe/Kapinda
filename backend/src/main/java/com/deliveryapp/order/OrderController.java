package com.deliveryapp.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;
import com.deliveryapp.account.AccountRole;
import com.deliveryapp.account.AccountService;
import com.deliveryapp.account.AuthenticatedUser;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orderService;
  private final AccountService accountService;

  public OrderController(OrderService orderService, AccountService accountService) {
    this.orderService = orderService;
    this.accountService = accountService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse create(@RequestAttribute AuthenticatedUser authenticatedUser, @Valid @RequestBody CreateOrderRequest request) {
    accountService.requireActive(authenticatedUser);
    return orderService.create(authenticatedUser.uid(), request);
  }

  @GetMapping("/{id}")
  public OrderResponse findById(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String id) {
    accountService.requireActive(authenticatedUser);
    return orderService.findByIdForCustomer(authenticatedUser.uid(), id);
  }

  @GetMapping
  public java.util.List<OrderResponse> history(@RequestAttribute AuthenticatedUser authenticatedUser) {
    accountService.requireActive(authenticatedUser);
    return orderService.findAllForCustomer(authenticatedUser.uid());
  }
}
