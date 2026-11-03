package com.deliveryapp.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminAccountController {
  private final AccountAdministrationService service;
  public AdminAccountController(AccountAdministrationService service) { this.service = service; }
  public record AccessRequest(@NotNull AccountRole role, @NotNull @Size(max = 100) Set<String> restaurantIds) {}
  @GetMapping
  public List<AccountResponse> search(@RequestAttribute AuthenticatedUser authenticatedUser, @RequestParam(defaultValue = "") String search,
                                      @RequestParam(required = false) AccountRole role) {
    return service.search(authenticatedUser, search, role);
  }
  @PutMapping("/{uid}/access")
  public AccountResponse assign(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String uid, @Valid @RequestBody AccessRequest request) {
    return service.assign(authenticatedUser, uid, request.role(), request.restaurantIds());
  }
}
