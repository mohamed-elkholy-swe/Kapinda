package com.deliveryapp.order;

import com.deliveryapp.account.*;
import com.deliveryapp.restaurant.*;
import com.deliveryapp.shared.ApiExceptionHandler;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckoutRequirementsTest {
  private Clock clock(String value) { return Clock.fixed(Instant.parse(value), ZoneOffset.UTC); }
  private CreateOrderRequest request(String name) {
    return new CreateOrderRequest(name,"01012345678","Customer address","City",List.of(new CreateOrderItemRequest("r","i",1)));
  }

  @ParameterizedTest
  @CsvSource({
    "2026-11-17T01:59:59Z,true", "2026-11-17T02:00:00Z,false",
    "2026-11-17T13:59:59Z,false", "2026-11-17T14:00:00Z,true",
    "2026-11-17T22:00:00Z,true", "2026-07-17T00:59:59Z,true",
    "2026-07-17T01:00:00Z,false", "2026-07-17T12:59:59Z,false",
    "2026-07-17T13:00:00Z,true"
  })
  void respectsCairoBoundariesAndDaylightSaving(String instant, boolean open) {
    assertEquals(open,OrderingHours.isOpen(clock(instant)));
  }

  @Test void rejectsClosedHoursAndBlankNamesBeforeSavingOrNotifying() {
    var repository=mock(OrderRepository.class); var restaurants=mock(RestaurantService.class);
    var profiles=mock(AccountRepository.class); var areas=mock(DeliveryAreaRepository.class);
    var closed=new OrderService(restaurants,repository,profiles,areas,clock("2026-11-17T02:00:00Z"));
    for (var uid : new String[]{null,"buyer"}) {
      assertEquals(OrderingHours.NOTICE,assertThrows(IllegalArgumentException.class,()->closed.create(uid,request("Ahmed"))).getMessage());
    }
    var open=new OrderService(restaurants,repository,profiles,areas,clock("2026-11-17T14:00:00Z"));
    for (var name : new String[]{null,"","   ","\t\n","a".repeat(101)})
      assertThrows(IllegalArgumentException.class,()->open.create("buyer",request(name)));
    verifyNoInteractions(repository,restaurants,profiles,areas);
  }

  @Test void savesCheckoutNameForGuestsAndMembersAtOpeningTime() {
    var repository=mock(OrderRepository.class); var restaurants=mock(RestaurantService.class);
    var areas=mock(DeliveryAreaRepository.class); var profiles=mock(AccountRepository.class);
    var area=new DeliveryArea();area.update("City",18);
    when(areas.findByName("City")).thenReturn(Optional.of(area));
    var restaurant=new RestaurantEntity("r","Kitchen","food","New","30 min",0,"restaurant");
    var item=new MenuItemEntity("i","Meal","",50);restaurant.addMenuItem(item);
    when(restaurants.findEntityById("r")).thenReturn(restaurant);
    when(restaurants.findMenuItemEntity("r","i")).thenReturn(item);
    when(repository.save(any())).thenAnswer(invocation->invocation.getArgument(0));
    var service=new OrderService(restaurants,repository,profiles,areas,clock("2026-11-17T14:00:00Z"));
    for (var uid : new String[]{null,"buyer"}) {
      var response=service.create(uid,request("  أحمد علي  "));
      assertEquals("أحمد علي",response.customerName());
      assertEquals(Instant.parse("2026-11-17T14:00:00Z"),response.createdAt());
    }
    verifyNoInteractions(profiles);
  }

  @Test void apiRequiresNameForGuestRequestsAndReturnsBilingualValidationMessage() throws Exception {
    var service=mock(OrderService.class);
    var mvc=MockMvcBuilders.standaloneSetup(new OrderController(service,mock(AccountService.class)))
      .setControllerAdvice(new ApiExceptionHandler()).build();
    var mapper=new com.fasterxml.jackson.databind.ObjectMapper();
    for (var name : new String[]{null,"","  ","a".repeat(101)}) {
      var body=new HashMap<String,Object>();
      if(name!=null) body.put("customerName",name);
      body.put("customerPhone","01012345678");body.put("customerAddress","Customer address");body.put("area","City");
      body.put("items",List.of(Map.of("restaurantId","r","menuItemId","i","quantity",1)));
      mvc.perform(post("/api/orders").contentType("application/json").content(mapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(" / ")));
    }
    verifyNoInteractions(service);
  }

  @Test void savedOrderNameTakesPriorityOverLaterProfileChanges() {
    var profiles=mock(AccountRepository.class);
    var service=new OrderService(mock(RestaurantService.class),mock(OrderRepository.class),profiles,mock(DeliveryAreaRepository.class));
    var order=new OrderEntity(OrderStatus.PLACED,"buyer","phone","address","City",50,18,68,Instant.now());
    order.setCustomerName("Original checkout name");
    assertEquals("Original checkout name",service.toResponse(order).customerName());
    verifyNoInteractions(profiles);
  }
}
