package com.deliveryapp.restaurant;

import java.util.List;
import java.util.Locale;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** User-requested demo dishes and representative stock photographs. */
@Component
@Order(100)
public class SampleMenuSeeder implements CommandLineRunner {
  private final RestaurantRepository repository;
  public SampleMenuSeeder(RestaurantRepository repository) { this.repository = repository; }
  private record Dish(String name, String category, int price, String image) {}
  private static final String PIZZA = "photo-1579751626657-72bc17010498";
  private static final String BURGER = "photo-1568901346375-23c9450c58cd";
  private static final String GRILL = "photo-1544025162-d76694265947";
  private static final String CHICKEN = "photo-1532550907401-a500c9a57435";
  private static final String FRIES = "photo-1573080496219-bb080dd4f877";
  private static final String SWEET = "photo-1578985545062-69928b1d9587";
  private static final String DRINK = "photo-1544145945-f90425340c7e";
  private static String photo(String image) {
    return "https://images.unsplash.com/" + image + "?auto=format&fit=crop&w=640&q=80";
  }
  private List<Dish> dishes(RestaurantEntity restaurant) {
    return switch (restaurant.getCategory().toLowerCase(Locale.ROOT)) {
      case "pizza" -> List.of(
        new Dish("بيتزا مارجريتا تجريبية", "بيتزا وفطائر", 120, PIZZA),
        new Dish("فطيرة جبنة تجريبية", "بيتزا وفطائر", 95, PIZZA),
        new Dish("بطاطس بالجبنة تجريبية", "مقبلات", 50, FRIES),
        new Dish("بطاطس مقرمشة تجريبية", "مقبلات", 40, FRIES),
        new Dish("عصير ليمون تجريبي", "مشروبات وحلو", 35, DRINK),
        new Dish("كيك شوكولاتة تجريبي", "مشروبات وحلو", 65, SWEET));
      case "dessert" -> List.of(
        new Dish("كيك شوكولاتة تجريبي", "تورت وكيك", 90, SWEET),
        new Dish("قطعة كيك فانيليا تجريبية", "تورت وكيك", 70, SWEET),
        new Dish("طبق حلويات مشكل تجريبي", "حلويات شرقية", 110, SWEET),
        new Dish("حلوى بالمكسرات تجريبية", "حلويات شرقية", 80, SWEET),
        new Dish("عصير برتقال تجريبي", "مشروبات", 40, DRINK),
        new Dish("ليمون نعناع تجريبي", "مشروبات", 45, DRINK));
      case "grill" -> List.of(
        new Dish("وجبة مشويات مشكلة تجريبية", "مشويات", 240, GRILL),
        new Dish("وجبة فراخ مشوية تجريبية", "مشويات", 180, CHICKEN),
        new Dish("بطاطس مقرمشة تجريبية", "مقبلات", 40, FRIES),
        new Dish("بطاطس بالجبنة تجريبية", "مقبلات", 55, FRIES),
        new Dish("عصير ليمون تجريبي", "مشروبات وحلو", 35, DRINK),
        new Dish("كيك شوكولاتة تجريبي", "مشروبات وحلو", 65, SWEET));
      default -> List.of(
        new Dish("برجر كلاسيك تجريبي", "ساندوتشات", 130, BURGER),
        new Dish("برجر دبل تجريبي", "ساندوتشات", 170, BURGER),
        new Dish("بطاطس مقرمشة تجريبية", "مقبلات", 40, FRIES),
        new Dish("بطاطس بالجبنة تجريبية", "مقبلات", 55, FRIES),
        new Dish("ليمون نعناع تجريبي", "مشروبات وحلو", 40, DRINK),
        new Dish("كيك شوكولاتة تجريبي", "مشروبات وحلو", 65, SWEET));
    };
  }
  private String category(MenuItemEntity item, List<Dish> examples) {
    String name = item.getName();
    if (name.matches(".*(بطاطس|أرز|ارز|سلطة|ملوخية|بامية|تورلي|مقبلات).*")) return examples.get(2).category();
    if (name.matches(".*(عصير|ليمون|مشروب|كيك|وافل|كنافة|لبن).*")) return examples.get(4).category();
    return examples.get(0).category();
  }
  @Override @Transactional
  public void run(String... args) {
    for (RestaurantEntity restaurant : repository.findAll()) {
      if (restaurant.getSampleMenuVersion() != null && restaurant.getSampleMenuVersion() >= 1) continue;
      List<Dish> examples = dishes(restaurant);
      for (MenuItemEntity item : restaurant.getMenu()) {
        if (item.getCategory() == null || item.getCategory().isBlank()) item.setCategory(category(item, examples));
        if (item.getPhotoUrl() == null || item.getPhotoUrl().isBlank()) {
          String image = examples.get(0).image();
          if (item.getCategory().equals(examples.get(2).category())) image = examples.get(2).image();
          if (item.getCategory().equals(examples.get(4).category())) image = item.getName().matches(".*(كيك|وافل|كنافة|لبن).*") ? SWEET : DRINK;
          if (item.getName().contains("فراخ") || item.getName().contains("فرخة")) image = CHICKEN;
          item.setPhotoUrl(photo(image));
        }
      }
      for (int index = 0; index < examples.size(); index++) {
        Dish dish = examples.get(index);
        String id = restaurant.getId() + "-sample-v1-" + (index + 1);
        if (restaurant.getMenu().stream().anyMatch(item -> item.getId().equals(id))) continue;
        var item = new MenuItemEntity(id, dish.name(), "صنف وسعر للتجربة فقط — صورة توضيحية وليست صورة فعلية لمنتج المطعم.", dish.price());
        item.setCategory(dish.category());
        item.setPhotoUrl(photo(dish.image()));
        restaurant.addMenuItem(item);
      }
      restaurant.setSampleMenuVersion(1);
      repository.save(restaurant);
    }
  }
}
