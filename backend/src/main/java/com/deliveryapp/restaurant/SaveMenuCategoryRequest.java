package com.deliveryapp.restaurant;
public record SaveMenuCategoryRequest(
  @com.fasterxml.jackson.annotation.JsonAlias("name") @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 100) String nameAr,
  @jakarta.validation.constraints.Size(max = 100) String nameEn
) {
  public SaveMenuCategoryRequest(String name) { this(name, null); }
}
