package com.deliveryapp.restaurant;

import com.deliveryapp.account.AccountRole;
import com.deliveryapp.account.AccountService;
import com.deliveryapp.account.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/restaurants")
public class AdminRestaurantController {
  private final RestaurantService restaurantService;
  private final AccountService accountService;

  public AdminRestaurantController(RestaurantService restaurantService, AccountService accountService) {
    this.restaurantService = restaurantService;
    this.accountService = accountService;
  }

  @PostMapping("/{restaurantId}/menu-categories")
  public Restaurant addCategory(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String restaurantId, @Valid @RequestBody SaveMenuCategoryRequest request) {
    requireAdmin(authenticatedUser);
    return restaurantService.addMenuCategory(restaurantId, request.name());
  }

  @GetMapping
  public List<Restaurant> findAll(@RequestAttribute AuthenticatedUser authenticatedUser) {
    requireAdmin(authenticatedUser);
    return restaurantService.findAll(null, null);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Restaurant create(@RequestAttribute AuthenticatedUser authenticatedUser, @Valid @RequestBody SaveRestaurantRequest request) {
    requireAdmin(authenticatedUser);
    return restaurantService.create(request);
  }

  @PutMapping("/{restaurantId}")
  public Restaurant update(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String restaurantId, @Valid @RequestBody SaveRestaurantRequest request) {
    requireAdmin(authenticatedUser);
    return restaurantService.update(restaurantId, request);
  }

  @DeleteMapping("/{restaurantId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String restaurantId) {
    requireAdmin(authenticatedUser);
    restaurantService.delete(restaurantId);
  }

  @PostMapping("/{restaurantId}/menu")
  @ResponseStatus(HttpStatus.CREATED)
  public MenuItem addMenuItem(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String restaurantId, @Valid @RequestBody SaveMenuItemRequest request) {
    requireAdmin(authenticatedUser);
    return restaurantService.addMenuItem(restaurantId, request);
  }

  @PutMapping("/{restaurantId}/menu/{itemId}")
  public MenuItem updateMenuItem(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String restaurantId, @PathVariable String itemId, @Valid @RequestBody SaveMenuItemRequest request) {
    requireAdmin(authenticatedUser);
    return restaurantService.updateMenuItem(restaurantId, itemId, request);
  }

  @DeleteMapping("/{restaurantId}/menu/{itemId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMenuItem(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String restaurantId, @PathVariable String itemId) {
    requireAdmin(authenticatedUser);
    restaurantService.deleteMenuItem(restaurantId, itemId);
  }

  private void requireAdmin(AuthenticatedUser user) {
    accountService.requireRole(user, AccountRole.ADMIN);
  }
}
