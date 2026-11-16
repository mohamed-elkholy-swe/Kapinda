package com.deliveryapp.order;
import com.deliveryapp.account.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
class RestaurantOrderInboxTest {
 @Test void includesAssignedOrdersButRedactsOtherRestaurantsAndCustomerContact(){
  var accounts=mock(AccountService.class);var repository=mock(OrderRepository.class);var profiles=mock(AccountRepository.class);
  var service=new OrderService(mock(com.deliveryapp.restaurant.RestaurantService.class),repository,profiles,mock(DeliveryAreaRepository.class));
  var user=new AuthenticatedUser("staff","staff@example.test");var account=new AccountEntity("staff","staff@example.test",AccountRole.RESTAURANT,AccountStatus.ACTIVE);account.assignAccess(AccountRole.RESTAURANT,Set.of("mine"));
  when(accounts.requireRole(user,AccountRole.RESTAURANT)).thenReturn(account);
  var order=new OrderEntity(OrderStatus.PLACED,"buyer","private-phone","private-address","City",140,18,158,Instant.now());
  order.addItem(new OrderItemEntity("mine","My kitchen","dish","Pizza",2,50,100));order.addItem(new OrderItemEntity("other","Other kitchen","secret","Other dish",1,40,40));
  when(repository.findAssignedRestaurantOrders(Set.of("mine"))).thenReturn(List.of(order));
  var buyer=new AccountEntity("buyer","buyer@example.test",AccountRole.CUSTOMER,AccountStatus.ACTIVE);buyer.updateProfile("Ahmed Ali","private-phone");
  var courier=new AccountEntity("courier","courier@example.test",AccountRole.DELIVERY,AccountStatus.ACTIVE);courier.updateProfile("Mohamed Hassan","01012345678");
  order.assignDeliveryPerson("courier");
  when(profiles.findById("buyer")).thenReturn(Optional.of(buyer));when(profiles.findById("courier")).thenReturn(Optional.of(courier));
  var result=new RestaurantOrdersController(accounts,repository,service).list(user);
  assertTrue(result.get(0).restaurantCanUpdate());assertEquals(OrderStatus.PLACED,result.get(0).items().get(0).restaurantStatus());
  assertEquals("Ahmed Ali",result.get(0).customerName());assertEquals(1,result.size());assertEquals(1,result.get(0).items().size());assertEquals("Pizza",result.get(0).items().get(0).itemName());assertEquals(100,result.get(0).subtotal());assertEquals("",result.get(0).customerPhone());assertEquals("",result.get(0).customerAddress());
  assertTrue(result.get(0).deliveryAssigned());assertEquals("Mohamed Hassan",result.get(0).deliveryPersonName());assertEquals("01012345678",result.get(0).deliveryPersonPhone());
 }
 @Test void noAssignmentsReturnsEmptyAndCustomersCannotReadRestaurantOrders(){
  var accounts=mock(AccountService.class);var repository=mock(OrderRepository.class);var controller=new RestaurantOrdersController(accounts,repository,mock(OrderService.class));var user=new AuthenticatedUser("u","u@example.test");
  when(accounts.requireRole(user,AccountRole.RESTAURANT)).thenReturn(new AccountEntity("u","u@example.test",AccountRole.RESTAURANT,AccountStatus.ACTIVE));assertTrue(controller.list(user).isEmpty());verifyNoInteractions(repository);
  when(accounts.requireRole(user,AccountRole.RESTAURANT)).thenThrow(new SecurityException());assertThrows(SecurityException.class,()->controller.list(user));
 }

 @Test void staffCanAdvanceTheirKitchenInMixedOrdersButCannotEditOtherRestaurants(){
  var accounts=mock(AccountService.class);var repository=mock(OrderRepository.class);
  var service=new OrderService(mock(com.deliveryapp.restaurant.RestaurantService.class),repository,mock(AccountRepository.class),mock(DeliveryAreaRepository.class));
  var controller=new RestaurantOrdersController(accounts,repository,service);
  var user=new AuthenticatedUser("staff","staff@example.test");var account=new AccountEntity("staff","staff@example.test",AccountRole.RESTAURANT,AccountStatus.ACTIVE);account.assignAccess(AccountRole.RESTAURANT,Set.of("mine"));when(accounts.requireRole(user,AccountRole.RESTAURANT)).thenReturn(account);
  var order=new OrderEntity(OrderStatus.PLACED,"buyer","phone","address","City",100,18,118,Instant.now());
  order.addItem(new OrderItemEntity("mine","Kitchen","dish","Dish",1,50,50));
  order.addItem(new OrderItemEntity("other","Other kitchen","other","Other dish",1,50,50));
  when(repository.lockById(1L)).thenReturn(Optional.of(order));
  assertThrows(IllegalArgumentException.class,()->controller.updateStatus(user,1L,OrderStatus.READY,"mine"));
  controller.updateStatus(user,1L,OrderStatus.ACCEPTED,null);
  controller.updateStatus(user,1L,OrderStatus.PREPARING,"mine");
  controller.updateStatus(user,1L,OrderStatus.READY,"mine");
  assertEquals(OrderStatus.READY,order.getItems().get(0).getRestaurantStatus());
  assertEquals(OrderStatus.PLACED,order.getItems().get(1).getRestaurantStatus());
  assertEquals(OrderStatus.PLACED,order.getStatus());
  assertThrows(IllegalArgumentException.class,()->controller.updateStatus(user,1L,OrderStatus.DELIVERED,"mine"));
  assertThrows(SecurityException.class,()->controller.updateStatus(user,1L,OrderStatus.ACCEPTED,"other"));
  account.assignAccess(AccountRole.RESTAURANT,Set.of("mine","other"));
  assertThrows(IllegalArgumentException.class,()->controller.updateStatus(user,1L,OrderStatus.ACCEPTED,null));
  controller.updateStatus(user,1L,OrderStatus.ACCEPTED,"other");
  assertEquals(OrderStatus.ACCEPTED,order.getStatus());
  account.assignAccess(AccountRole.RESTAURANT,Set.of());
  assertThrows(SecurityException.class,()->controller.updateStatus(user,1L,OrderStatus.READY,"mine"));
 }
}
