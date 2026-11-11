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
import com.deliveryapp.account.AccountService;
import com.deliveryapp.account.AuthenticatedUser;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  static final java.time.Instant LAUNCH_AT = java.time.OffsetDateTime.parse("2026-11-12T16:00:00+02:00").toInstant();
  private final java.time.Clock clock;
  private final OrderService orderService;
  private final AccountService accountService;

  @org.springframework.beans.factory.annotation.Autowired
  public OrderController(OrderService orderService, AccountService accountService) {
    this(orderService, accountService, java.time.Clock.systemUTC());
  }

  public OrderController(OrderService orderService, AccountService accountService, java.time.Clock clock) {
    this.orderService = orderService;
    this.accountService = accountService;
    this.clock = clock;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse create(@RequestAttribute(required = false) AuthenticatedUser authenticatedUser, @Valid @RequestBody CreateOrderRequest request) {
    var account = authenticatedUser == null ? null : accountService.requireActive(authenticatedUser);
    if (clock.instant().isBefore(LAUNCH_AT) && (account == null || !account.getRole().isAdministrator())) {
      throw new SecurityException("Orders open Thursday, November 12 at 4:00 PM Egypt time. / الطلبات هتبدأ يوم الخميس ١٢ نوفمبر الساعة ٤ مساءً بتوقيت مصر.");
    }
    return orderService.create(authenticatedUser == null ? null : authenticatedUser.uid(), request);
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
