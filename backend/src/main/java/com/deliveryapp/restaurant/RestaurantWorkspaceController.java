package com.deliveryapp.restaurant;

import com.deliveryapp.account.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-workspace/restaurants")
public class RestaurantWorkspaceController {
  private final AccountService accounts;
  private final RestaurantService restaurants;
  public RestaurantWorkspaceController(AccountService accounts, RestaurantService restaurants) { this.accounts = accounts; this.restaurants = restaurants; }
  @PostMapping("/{restaurantId}/menu-categories")
  public Restaurant addCategory(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String restaurantId, @Valid @RequestBody SaveMenuCategoryRequest request) {
    accounts.requireRestaurant(authenticatedUser, restaurantId);
    return restaurants.addMenuCategory(restaurantId, request.name());
  }

  @GetMapping
  public List<Restaurant> list(@RequestAttribute AuthenticatedUser authenticatedUser) {
    var account = accounts.requireRole(authenticatedUser, AccountRole.RESTAURANT);
    var all = restaurants.findAll(null, null);
    return account.getRole().isAdministrator() ? all : all.stream().filter(r -> account.getRestaurantIds().contains(r.id())).toList();
  }
  @PutMapping("/{id}")
  public Restaurant update(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String id, @Valid @RequestBody SaveRestaurantRequest request) {
    accounts.requireRestaurant(authenticatedUser, id);
    return restaurants.update(id, request);
  }
  @PostMapping("/{id}/menu")
  public MenuItem addItem(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String id, @Valid @RequestBody SaveMenuItemRequest request) {
    accounts.requireRestaurant(authenticatedUser, id);
    return restaurants.addMenuItem(id, request);
  }
  @PutMapping("/{id}/menu/{itemId}")
  public MenuItem updateItem(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String id, @PathVariable String itemId, @Valid @RequestBody SaveMenuItemRequest request) {
    accounts.requireRestaurant(authenticatedUser, id);
    return restaurants.updateMenuItem(id, itemId, request);
  }
  @DeleteMapping("/{id}/menu/{itemId}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void deleteItem(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String id, @PathVariable String itemId) {
    accounts.requireRestaurant(authenticatedUser, id);
    restaurants.deleteMenuItem(id, itemId);
  }
}
