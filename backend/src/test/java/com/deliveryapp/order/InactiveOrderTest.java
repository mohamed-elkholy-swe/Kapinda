package com.deliveryapp.order;

import com.deliveryapp.account.AccountRepository;
import com.deliveryapp.restaurant.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InactiveOrderTest {
  @Test void rejectsStaleBasketForInactiveRestaurantOrItemBeforeSaving() {
    var restaurants = mock(RestaurantService.class);
    var orders = mock(OrderRepository.class);
    var areas = mock(DeliveryAreaRepository.class);
    var area = new DeliveryArea(); area.update("City", 10);
    when(areas.findByName("City")).thenReturn(Optional.of(area));
    var restaurant = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    var item = new MenuItemEntity("i", "Pizza", "", 50);
    restaurant.addMenuItem(item);
    when(restaurants.findEntityById("r")).thenReturn(restaurant);
    when(restaurants.findMenuItemEntity("r", "i")).thenReturn(item);
    var service = new OrderService(restaurants, orders, mock(AccountRepository.class), areas);
    var request = new CreateOrderRequest("01000000000", "Customer address", "City", List.of(new CreateOrderItemRequest("r", "i", 1)));
    restaurant.setActive(false);
    assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class, () -> service.create("buyer", request)).getStatusCode());
    restaurant.setActive(true); item.setActive(false);
    assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class, () -> service.create("buyer", request)).getStatusCode());
    verifyNoInteractions(orders);
  }
}
