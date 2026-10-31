package com.deliveryapp.restaurant;
public record SaveMenuCategoryRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 100) String name) {}
