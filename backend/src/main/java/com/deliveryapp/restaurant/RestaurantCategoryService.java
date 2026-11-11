package com.deliveryapp.restaurant;

import java.text.Normalizer;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class RestaurantCategoryService {
  public record Category(String id, String nameAr, String nameEn) {}
  public record SaveCategory(@NotBlank @Size(max = 100) String nameAr, @Size(max = 100) String nameEn) {}
  private final RestaurantCategoryRepository repository;
  public RestaurantCategoryService(RestaurantCategoryRepository repository) { this.repository = repository; }

  public static String normalize(String value) {
    return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC)
      .replaceAll("[\\p{M}ـ]", "").replaceAll("[أإآٱ]", "ا").trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }
  public static String canonical(String value) {
    return switch (normalize(value)) {
      case "pizza", "pizzas", "بيتزا" -> "pizza";
      case "burger", "burgers", "برجر", "برغر" -> "burger";
      case "dessert", "desserts", "sweets", "حلويات" -> "dessert";
      case "grill", "grills", "grilled", "مشويات" -> "grill";
      case "chicken", "دجاج", "فراخ" -> "chicken";
      case "egyptian", "اكل مصري", "مصري" -> "egyptian";
      default -> normalize(value);
    };
  }
  private static String[] names(String key, String original) {
    return switch (key) {
      case "pizza" -> new String[]{"بيتزا", "Pizza"};
      case "burger" -> new String[]{"برجر", "Burgers"};
      case "dessert" -> new String[]{"حلويات", "Desserts"};
      case "grill" -> new String[]{"مشويات", "Grills"};
      case "chicken" -> new String[]{"دجاج", "Chicken"};
      case "egyptian" -> new String[]{"أكل مصري", "Egyptian"};
      default -> new String[]{original.trim(), null};
    };
  }
  private Optional<RestaurantCategoryEntity> find(String value) {
    if (value == null || value.isBlank()) return Optional.empty();
    var key = normalize(value);
    return repository.findById(canonical(value)).or(() -> repository.findByNameArKeyOrNameEnKey(key, key));
  }
  private Category response(RestaurantCategoryEntity entity) { return new Category(entity.getId(), entity.getNameAr(), entity.getNameEn()); }
  @Transactional(readOnly = true)
  public List<Category> list() { return repository.findAll().stream().sorted(Comparator.comparing(RestaurantCategoryEntity::getNameAr)).map(this::response).toList(); }
  @Transactional(readOnly = true)
  public String requireId(String value) {
    return find(value).map(RestaurantCategoryEntity::getId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a saved restaurant category"));
  }
  @Transactional
  public Category create(SaveCategory request) {
    var existingAr = find(request.nameAr());
    var existingEn = find(request.nameEn());
    if (existingAr.isPresent() && existingEn.isPresent() && !existingAr.get().getId().equals(existingEn.get().getId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "These names belong to different categories");
    }
    var existing = existingAr.or(() -> existingEn);
    if (existing.isPresent()) return response(existing.get());
    String key = canonical(request.nameAr());
    var known = names(key, request.nameAr());
    String id = known[1] != null ? key : UUID.randomUUID().toString();
    return response(repository.save(new RestaurantCategoryEntity(id, request.nameAr(), request.nameEn())));
  }
  /** Convert existing category labels to shared IDs without creating restaurant records. */
  @Transactional
  public String importExisting(String value) {
    if (value == null || value.isBlank()) return value;
    var existing = find(value);
    if (existing.isPresent()) return existing.get().getId();
    var key = canonical(value);
    var labels = names(key, value);
    String id = labels[1] != null ? key : UUID.randomUUID().toString();
    return repository.save(new RestaurantCategoryEntity(id, labels[0], labels[1])).getId();
  }
}
