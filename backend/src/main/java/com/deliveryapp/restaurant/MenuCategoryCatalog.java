package com.deliveryapp.restaurant;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Saved categories belong to a single restaurant. Legacy string keys remain valid. */
public final class MenuCategoryCatalog {
  private MenuCategoryCatalog() {}
  public static List<MenuCategoryDefinition> list(RestaurantEntity restaurant) {
    var result = new LinkedHashMap<String, MenuCategoryDefinition>();
    var details = restaurant.getDetails();
    if (details != null && details.menuCategoryDefinitions != null) details.menuCategoryDefinitions.forEach(c -> result.put(c.id(), c));
    if (details != null && details.menuCategories != null) details.menuCategories.stream().filter(v -> v != null && !v.isBlank()).forEach(v -> result.putIfAbsent(v.trim(), new MenuCategoryDefinition(v.trim(), v.trim(), null)));
    restaurant.getMenu().stream().map(MenuItemEntity::getCategory).filter(v -> v != null && !v.isBlank()).forEach(v -> result.putIfAbsent(v.trim(), new MenuCategoryDefinition(v.trim(), v.trim(), null)));
    return new ArrayList<>(result.values());
  }
  public static String requireCategory(RestaurantEntity restaurant, String value) {
    if (value == null || value.isBlank()) return value == null ? null : "";
    return list(restaurant).stream().filter(c -> c.id().equals(value.trim())).findFirst().map(MenuCategoryDefinition::id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a saved menu category or create it first"));
  }
  private static boolean matches(MenuCategoryDefinition category, String name) {
    if (name == null || name.isBlank()) return false;
    String key = RestaurantCategoryService.normalize(name);
    return RestaurantCategoryService.normalize(category.nameAr()).equals(key)
      || category.nameEn() != null && RestaurantCategoryService.normalize(category.nameEn()).equals(key);
  }
  public static void add(RestaurantEntity restaurant, String nameAr, String nameEn) {
    var definitions = list(restaurant);
    var arabic = definitions.stream().filter(c -> matches(c, nameAr)).findFirst();
    var english = definitions.stream().filter(c -> matches(c, nameEn)).findFirst();
    if (arabic.isPresent() && english.isPresent() && !arabic.get().id().equals(english.get().id())) throw new ResponseStatusException(HttpStatus.CONFLICT, "These names belong to different menu categories");
    var existing = arabic.or(() -> english);
    if (existing.isEmpty()) {
      // Names are saved keys for compatibility with existing menu items; translations are separate.
      definitions.add(new MenuCategoryDefinition(nameAr.trim(), nameAr.trim(), nameEn == null || nameEn.isBlank() ? null : nameEn.trim()));
    } else if (existing.get().nameEn() == null && nameEn != null && !nameEn.isBlank()) {
      var old = existing.get();
      definitions.set(definitions.indexOf(old), new MenuCategoryDefinition(old.id(), nameAr.trim(), nameEn.trim()));
    }
    var details = restaurant.getDetails();
    if (details == null) details = new RestaurantDetails();
    details.menuCategoryDefinitions = definitions;
    details.menuCategories = definitions.stream().map(MenuCategoryDefinition::id).toList();
    restaurant.setDetails(details);
  }
}
