package com.deliveryapp.restaurant;

import com.deliveryapp.account.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class RestaurantCategoryController {
  private final RestaurantCategoryService categories;
  private final AccountService accounts;
  public RestaurantCategoryController(RestaurantCategoryService categories, AccountService accounts) { this.categories = categories; this.accounts = accounts; }
  @GetMapping("/api/restaurant-categories")
  public java.util.List<RestaurantCategoryService.Category> list() { return categories.list(); }
  @PostMapping("/api/admin/restaurant-categories")
  public RestaurantCategoryService.Category create(@RequestAttribute AuthenticatedUser authenticatedUser, @Valid @RequestBody RestaurantCategoryService.SaveCategory request) {
    accounts.requireRole(authenticatedUser, AccountRole.ADMIN);
    return categories.create(request);
  }
  @PostMapping("/api/restaurant-workspace/restaurant-categories")
  public RestaurantCategoryService.Category createForWorkspace(@RequestAttribute AuthenticatedUser authenticatedUser, @Valid @RequestBody RestaurantCategoryService.SaveCategory request) {
    accounts.requireRole(authenticatedUser, AccountRole.RESTAURANT);
    return categories.create(request);
  }
}
