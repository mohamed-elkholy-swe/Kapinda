package com.deliveryapp.restaurant;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class RestaurantService {
  private final RestaurantRepository restaurantRepository;
  private final MenuItemRepository menuItemRepository;

  public RestaurantService(RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository) {
    this.restaurantRepository = restaurantRepository;
    this.menuItemRepository = menuItemRepository;
  }

  @Transactional(readOnly = true)
  public List<Restaurant> findAll(String category, String search) {
    String normalizedCategory = normalize(category);
    String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

    return restaurantRepository.findAll().stream()
      .sorted(Comparator.comparing(RestaurantEntity::getName))
      .filter(restaurant ->
        normalizedCategory.isBlank()
          || "all".equals(normalizedCategory)
          || restaurant.getCategory().equals(normalizedCategory)
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
      UUID.randomUUID().toString(), request.name().trim(), normalize(request.category()),
      "New", request.deliveryTime().trim(), request.minimumOrder(), request.art().trim()
    );
    updatePhoto(entity, request.photoUrl());
    return toResponse(restaurantRepository.save(entity));
  }

  @Transactional
  public Restaurant update(String restaurantId, SaveRestaurantRequest request) {
    RestaurantEntity entity = findEntityById(restaurantId);
    entity.update(request.name().trim(), normalize(request.category()), request.deliveryTime().trim(), request.minimumOrder(), request.art().trim());
    updatePhoto(entity, request.photoUrl());
    return toResponse(entity);
  }

  @Transactional
  public MenuItem addMenuItem(String restaurantId, SaveMenuItemRequest request) {
    RestaurantEntity restaurant = findEntityById(restaurantId);
    MenuItemEntity item = new MenuItemEntity(UUID.randomUUID().toString(), request.name().trim(), (request.description() == null ? "" : request.description().trim()), request.price());
    if (request.photoUrl() != null) item.setPhotoUrl(request.photoUrl().trim());
    if (request.category() != null) item.setCategory(request.category().trim());
    restaurant.addMenuItem(item);
    return new MenuItem(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getPhotoUrl(), item.getCategory());
  }

  @Transactional
  public MenuItem updateMenuItem(String restaurantId, String itemId, SaveMenuItemRequest request) {
    RestaurantEntity restaurant = findEntityById(restaurantId);
    MenuItemEntity item = findMenuItemEntity(restaurant.getId(), itemId);
    item.update(request.name().trim(), (request.description() == null ? "" : request.description().trim()), request.price());
    if (request.photoUrl() != null) item.setPhotoUrl(request.photoUrl().trim());
    if (request.category() != null) item.setCategory(request.category().trim());
    return new MenuItem(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getPhotoUrl(), item.getCategory());
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

  private Restaurant toResponse(RestaurantEntity entity) {
    return new Restaurant(
      entity.getId(),
      entity.getName(),
      entity.getCategory(),
      entity.getRating(),
      entity.getDeliveryTime(),
      entity.getMinimumOrder(),
      entity.getArt(),
      entity.getMenu().stream()
        .map(item -> new MenuItem(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getPhotoUrl(), item.getCategory()))
        .toList(),
      entity.getDetails()
    );
  }

  @Transactional
  public Restaurant addMenuCategory(String restaurantId, String name) {
    RestaurantEntity entity = findEntityById(restaurantId);
    RestaurantDetails details = entity.getDetails();
    if (details == null) details = new RestaurantDetails();
    var categories = new java.util.ArrayList<String>(details.menuCategories == null ? java.util.List.of() : details.menuCategories);
    if (categories.stream().noneMatch(c -> c.equalsIgnoreCase(name.trim()))) categories.add(name.trim());
    details.menuCategories = categories;
    entity.setDetails(details);
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
    entity.getMenu().stream()
      .filter(item -> item.getCategory() != null && category.equals(item.getCategory().trim()))
      .forEach(item -> item.setCategory(null));
    return toResponse(entity);
  }

  private void updatePhoto(RestaurantEntity entity, String photoUrl) {
    if (photoUrl == null) return;
    RestaurantDetails details = entity.getDetails();
    if (details == null) details = new RestaurantDetails();
    details.photoUrl = photoUrl.trim();
    entity.setDetails(details);
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }
}
