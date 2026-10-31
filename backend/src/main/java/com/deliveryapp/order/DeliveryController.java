package com.deliveryapp.order;

import com.deliveryapp.account.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery/orders")
public class DeliveryController {
  private final AccountService accounts;
  private final DeliveryService delivery;
  public DeliveryController(AccountService accounts, DeliveryService delivery) { this.accounts = accounts; this.delivery = delivery; }
  @GetMapping
  public List<DeliveryService.Task> list(@RequestAttribute AuthenticatedUser authenticatedUser) {
    var account = accounts.requireRole(authenticatedUser, AccountRole.DELIVERY);
    return delivery.list(authenticatedUser.uid(), account.getRole().isAdministrator());
  }
  @PostMapping("/{id}/claim")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void claim(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable Long id) {
    accounts.requireRole(authenticatedUser, AccountRole.DELIVERY);
    delivery.claim(authenticatedUser.uid(), id);
  }
  @PutMapping("/{id}/status")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void status(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable Long id, @RequestParam OrderStatus status) {
    var account = accounts.requireRole(authenticatedUser, AccountRole.DELIVERY);
    delivery.update(authenticatedUser.uid(), id, status, account.getRole().isAdministrator());
  }
}
