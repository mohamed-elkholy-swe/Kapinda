package com.deliveryapp.restaurant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class MenuCategoryDefinitionsConverter implements AttributeConverter<List<MenuCategoryDefinition>, String> {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  public String convertToDatabaseColumn(List<MenuCategoryDefinition> value) {
    if (value == null) return null;
    try { return MAPPER.writeValueAsString(value); }
    catch (Exception e) { throw new IllegalArgumentException("Cannot encode menu categories", e); }
  }
  public List<MenuCategoryDefinition> convertToEntityAttribute(String value) {
    if (value == null || value.isBlank()) return List.of();
    try { return MAPPER.readValue(value, new TypeReference<List<MenuCategoryDefinition>>() {}); }
    catch (Exception e) { throw new IllegalArgumentException("Cannot decode menu categories", e); }
  }
}
