package com.deliveryapp.restaurant;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuCategoriesTest {
  private RestaurantEntity restaurant(String id) { return new RestaurantEntity(id, "Kitchen", "food", "New", "30", 0, "restaurant"); }

  @Test void savesBilingualCategoriesAndArabicOnlyCategoriesWithoutDuplicates() {
    var entity = restaurant("r");
    MenuCategoryCatalog.add(entity, " مشروبات ", " Drinks ");
    MenuCategoryCatalog.add(entity, "مَشروبات", "drinks");
    MenuCategoryCatalog.add(entity, "حلويات", null);
    var categories = MenuCategoryCatalog.list(entity);
    assertEquals(2, categories.size());
    assertEquals("مشروبات", categories.get(0).nameAr());
    assertEquals("Drinks", categories.get(0).nameEn());
    assertNull(categories.get(1).nameEn());
    assertEquals("مشروبات", MenuCategoryCatalog.requireCategory(entity, "مشروبات"));
    assertThrows(ResponseStatusException.class, () -> MenuCategoryCatalog.requireCategory(entity, "Unknown"));
    assertThrows(ResponseStatusException.class, () -> MenuCategoryCatalog.requireCategory(restaurant("other"), "مشروبات"));
  }
  @Test void legacyNamesCanGainTranslationsWithoutBreakingExistingItems() {
    var entity = restaurant("r");
    var item = new MenuItemEntity("dish", "Tea", "", 20);
    item.setCategory("Drinks"); entity.addMenuItem(item);
    MenuCategoryCatalog.add(entity, "مشروبات", "Drinks");
    var category = MenuCategoryCatalog.list(entity).get(0);
    assertEquals("Drinks", category.id());
    assertEquals("مشروبات", category.nameAr());
    assertEquals("Drinks", item.getCategory());
    assertEquals(1, MenuCategoryCatalog.list(entity).size());
    var converter = new MenuCategoryDefinitionsConverter();
    assertEquals(List.of(category), converter.convertToEntityAttribute(converter.convertToDatabaseColumn(List.of(category))));
    assertTrue(converter.convertToEntityAttribute(null).isEmpty());
  }
  @Test void categoryDeletionKeepsDishesAndRejectsUnknownCategoriesOnItemUpdates() {
    var entities = mock(RestaurantRepository.class);
    var items = mock(MenuItemRepository.class);
    var entity = restaurant("r");
    when(entities.findById("r")).thenReturn(Optional.of(entity));
    var service = new RestaurantService(entities, items, mock(RestaurantCategoryService.class));
    service.addMenuCategory("r", "مشروبات", "Drinks");
    var item = service.addMenuItem("r", new SaveMenuItemRequest("Tea", "", 20, null, "مشروبات"));
    var stored = entity.getMenu().get(0);
    when(items.findById(item.id())).thenReturn(Optional.of(stored));
    assertThrows(ResponseStatusException.class, () -> service.updateMenuItem("r", item.id(), new SaveMenuItemRequest("Tea", "", 20, null, "Unsaved")));
    service.deleteMenuCategory("r", "مشروبات");
    assertEquals(1, entity.getMenu().size()); assertNull(stored.getCategory());
    assertTrue(entity.getDetails().menuCategoryDefinitions.isEmpty());
    assertTrue(MenuCategoryCatalog.list(entity).isEmpty());
  }
  @Test void validatesRequiredArabicAndOptionalEnglish() {
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertFalse(validator.validate(new SaveMenuCategoryRequest("", "Drinks")).isEmpty());
      assertTrue(validator.validate(new SaveMenuCategoryRequest("مشروبات", null)).isEmpty());
      assertFalse(validator.validate(new SaveMenuCategoryRequest("مشروبات", "x".repeat(101))).isEmpty());
    }
  }
}
