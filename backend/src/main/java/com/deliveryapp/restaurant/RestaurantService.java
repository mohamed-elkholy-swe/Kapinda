package com.deliveryapp.restaurant;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class RestaurantService {
  @Transactional(readOnly = true)
  public List<RestaurantSummary> findPublicSummaries(String search) {
    return restaurantRepository.findPublicSummaries(search == null ? "" : search.trim().toLowerCase(Locale.ROOT));
  }

  private final RestaurantRepository restaurantRepository;
  private final MenuItemRepository menuItemRepository;
  private final RestaurantCategoryService categories;

  public RestaurantService(RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository, RestaurantCategoryService categories) {
    this.restaurantRepository = restaurantRepository;
    this.menuItemRepository = menuItemRepository;
    this.categories = categories;
  }

  @Transactional(readOnly = true)
  public List<Restaurant> findAll(String category, String search) {
    String normalizedCategory = RestaurantCategoryService.canonical(category);
    String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

    return restaurantRepository.findAll().stream()
      .sorted(Comparator.comparing(RestaurantEntity::getName))
      .filter(restaurant ->
        normalizedCategory.isBlank()
          || "all".equals(normalizedCategory)
          || RestaurantCategoryService.canonical(restaurant.getCategory()).equals(normalizedCategory)
      )
      .filter(restaurant ->
        normalizedSearch.isBlank()
          || restaurant.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
          || restaurant.getMenu().stream().anyMatch(item -> item.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
      )
      .map(this::toResponse)
      .toList();
  }

  @Transactional(readOnly = true)
  public Restaurant findById(String id) {
    return toResponse(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public RestaurantEntity findEntityById(String id) {
    return restaurantRepository.findById(id)
      .orElseThrow(() -> new RestaurantNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public MenuItemEntity findMenuItemEntity(String restaurantId, String menuItemId) {
    MenuItemEntity menuItem = menuItemRepository.findById(menuItemId)
      .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));

    if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
      throw new MenuItemNotFoundException(menuItemId);
    }

    return menuItem;
  }

  @Transactional
  public Restaurant create(SaveRestaurantRequest request) {
    RestaurantEntity entity = new RestaurantEntity(
      UUID.randomUUID().toString(), request.name().trim(), categories.requireId(request.category()),
      "New", request.deliveryTime().trim(), request.minimumOrder(), request.art().trim()
    );
    updatePhoto(entity, request.photoUrl());
    updateLocationAndHours(entity, request);
    return toResponse(restaurantRepository.save(entity));
  }

  @Transactional
  public Restaurant update(String restaurantId, SaveRestaurantRequest request) {
    RestaurantEntity entity = findEntityById(restaurantId);
    entity.update(request.name().trim(), categories.requireId(request.category()), request.deliveryTime().trim(), request.minimumOrder(), request.art().trim());
    updatePhoto(entity, request.photoUrl());
    updateLocationAndHours(entity, request);
    return toResponse(entity);
  }

  @Transactional
  public MenuItem addMenuItem(String restaurantId, SaveMenuItemRequest request) {
    RestaurantEntity restaurant = findEntityById(restaurantId);
    MenuItemEntity item = new MenuItemEntity(UUID.randomUUID().toString(), request.name().trim(), (request.description() == null ? "" : request.description().trim()), request.price());
    if (request.photoUrl() != null) item.setPhotoUrl(request.photoUrl().trim());
    if (request.category() != null) item.setCategory(MenuCategoryCatalog.requireCategory(restaurant, request.category()));
    restaurant.addMenuItem(item);
    return new MenuItem(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getPhotoUrl(), item.getCategory(), item.isActive());
  }

  @Transactional
  public MenuItem updateMenuItem(String restaurantId, String itemId, SaveMenuItemRequest request) {
    RestaurantEntity restaurant = findEntityById(restaurantId);
    MenuItemEntity item = findMenuItemEntity(restaurant.getId(), itemId);
    item.update(request.name().trim(), (request.description() == null ? "" : request.description().trim()), request.price());
    if (request.photoUrl() != null) item.setPhotoUrl(request.photoUrl().trim());
    if (request.category() != null) item.setCategory(MenuCategoryCatalog.requireCategory(restaurant, request.category()));
    return new MenuItem(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getPhotoUrl(), item.getCategory(), item.isActive());
  }

  @Transactional
  public void deleteMenuItem(String restaurantId, String itemId) {
    RestaurantEntity restaurant = findEntityById(restaurantId);
    MenuItemEntity item = findMenuItemEntity(restaurant.getId(), itemId);
    restaurant.removeMenuItem(item);
  }

  @Transactional
  public void delete(String restaurantId) {
    restaurantRepository.delete(findEntityById(restaurantId));
  }

  @Transactional
  public Restaurant setActive(String restaurantId, boolean active) {
    RestaurantEntity entity = findEntityById(restaurantId);
    entity.setActive(active);
    return toResponse(entity);
  }

  @Transactional
  public MenuItem setMenuItemActive(String restaurantId, String itemId, boolean active) {
    MenuItemEntity item = findMenuItemEntity(restaurantId, itemId);
    item.setActive(active);
    return new MenuItem(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getPhotoUrl(), item.getCategory(), item.isActive());
  }

  @Transactional(readOnly = true)
  public List<Restaurant> findPublic(String category, String search) {
    String query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
    return findAll(category, null).stream().filter(Restaurant::active)
      .map(this::publicResponse)
      .filter(r -> query.isBlank() || r.name().toLowerCase(Locale.ROOT).contains(query)
        || r.menu().stream().anyMatch(i -> i.name().toLowerCase(Locale.ROOT).contains(query)))
      .toList();
  }

  @Transactional(readOnly = true)
  public Restaurant findPublicById(String id) {
    Restaurant restaurant = findById(id);
    if (!restaurant.active()) throw new RestaurantNotFoundException(id);
    return publicResponse(restaurant);
  }

  private Restaurant publicResponse(Restaurant r) {
    return new Restaurant(r.id(), r.name(), r.category(), r.rating(), r.deliveryTime(), r.minimumOrder(), r.art(),
      r.menu().stream().filter(MenuItem::active).toList(), r.details(), r.active(), r.ratingCount());
  }

  private Restaurant toResponse(RestaurantEntity entity) {
    return new Restaurant(
      entity.getId(),
      entity.getName(),
      entity.getCategory(),
      entity.getRatingCount() == 0 ? "New" : entity.getRating(),
      entity.getDeliveryTime(),
      entity.getMinimumOrder(),
      entity.getArt(),
      entity.getMenu().stream()
        .map(item -> new MenuItem(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getPhotoUrl(), item.getCategory(), item.isActive()))
        .toList(),
      entity.getDetails(),
      entity.isActive(),
      entity.getRatingCount()
    );
  }

  @Transactional
  public Restaurant addMenuCategory(String restaurantId, String name) {
    return addMenuCategory(restaurantId, name, null);
  }

  @Transactional
  public Restaurant addMenuCategory(String restaurantId, String nameAr, String nameEn) {
    RestaurantEntity entity = findEntityById(restaurantId);
    MenuCategoryCatalog.add(entity, nameAr, nameEn);
    return toResponse(entity);
  }

  @Transactional
  public Restaurant deleteMenuCategory(String restaurantId, String name) {
    RestaurantEntity entity = findEntityById(restaurantId);
    String category = name.trim();
    if (category.isEmpty()) throw new IllegalArgumentException("Category name is required");
    RestaurantDetails details = entity.getDetails();
    if (details != null && details.menuCategories != null) {
      details.menuCategories = details.menuCategories.stream()
        .filter(value -> !category.equals(value.trim())).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }
    if (details != null && details.menuCategoryDefinitions != null) details.menuCategoryDefinitions = details.menuCategoryDefinitions.stream().filter(c -> !category.equals(c.id())).toList();
    entity.getMenu().stream()
      .filter(item -> item.getCategory() != null && category.equals(item.getCategory().trim()))
      .forEach(item -> item.setCategory(null));
    return toResponse(entity);
  }

  private void updateLocationAndHours(RestaurantEntity entity, SaveRestaurantRequest request) {
    RestaurantDetails details = entity.getDetails();
    if (details == null) details = new RestaurantDetails();
    if (request.address() != null) details.address = request.address().trim();
    if (request.openingTime() != null) details.openingTime = request.openingTime();
    if (request.closingTime() != null) details.closingTime = request.closingTime();
    if (request.menuImages() != null) details.menuImages = request.menuImages().stream().map(String::trim).distinct().toList();
    entity.setDetails(details);
  }

  private void updatePhoto(RestaurantEntity entity, String photoUrl) {
    if (photoUrl == null) return;
    RestaurantDetails details = entity.getDetails();
    if (details == null) details = new RestaurantDetails();
    details.photoUrl = photoUrl.trim();
    entity.setDetails(details);
  }

}
