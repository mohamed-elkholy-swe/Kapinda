package com.deliveryapp.shared;

import com.deliveryapp.order.OrderNotFoundException;
import com.deliveryapp.restaurant.MenuItemNotFoundException;
import com.deliveryapp.restaurant.RestaurantNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({
    RestaurantNotFoundException.class,
    MenuItemNotFoundException.class,
    OrderNotFoundException.class
  })
  public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException exception) {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(error(HttpStatus.NOT_FOUND, exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
    return ResponseEntity
      .badRequest()
      .body(error(HttpStatus.BAD_REQUEST, "Invalid request data"));
  }

  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<Map<String, Object>> handleForbidden(SecurityException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(HttpStatus.FORBIDDEN, exception.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException exception) {
    return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, exception.getMessage()));
  }

  private Map<String, Object> error(HttpStatus status, String message) {
    return Map.of(
      "timestamp", Instant.now().toString(),
      "status", status.value(),
      "error", status.getReasonPhrase(),
      "message", message
    );
  }
}
