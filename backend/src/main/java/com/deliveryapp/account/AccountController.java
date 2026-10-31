package com.deliveryapp.account;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountController {
  private final AccountService service;
  public AccountController(AccountService service) { this.service = service; }

  @GetMapping("/me")
  public AccountResponse me(@RequestAttribute AuthenticatedUser authenticatedUser) {
    return AccountResponse.from(service.getOrCreate(authenticatedUser));
  }

  @PutMapping("/me")
  public AccountResponse update(@RequestAttribute AuthenticatedUser authenticatedUser, @Valid @RequestBody UpdateProfileRequest request) {
    return AccountResponse.from(service.updateProfile(authenticatedUser, request));
  }
}
