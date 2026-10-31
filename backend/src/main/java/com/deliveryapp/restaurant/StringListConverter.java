package com.deliveryapp.restaurant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  public String convertToDatabaseColumn(List<String> value) {
    if (value == null) return null;
    try { return MAPPER.writeValueAsString(value); }
    catch (Exception e) { throw new IllegalArgumentException("Cannot encode menu images", e); }
  }
  public List<String> convertToEntityAttribute(String value) {
    if (value == null || value.isBlank()) return List.of();
    try { return MAPPER.readValue(value, new TypeReference<List<String>>() {}); }
    catch (Exception e) { throw new IllegalArgumentException("Cannot decode menu images", e); }
  }
}
