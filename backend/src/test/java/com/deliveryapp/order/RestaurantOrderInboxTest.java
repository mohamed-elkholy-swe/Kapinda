package com.deliveryapp.order;
import com.deliveryapp.account.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
class RestaurantOrderInboxTest {
 @Test void includesAssignedOrdersButRedactsOtherRestaurantsAndCustomerContact(){
  var accounts=mock(AccountService.class);var repository=mock(OrderRepository.class);var service=mock(OrderService.class);
  var user=new AuthenticatedUser("staff","staff@example.test");var account=new AccountEntity("staff","staff@example.test",AccountRole.RESTAURANT,AccountStatus.ACTIVE);account.assignAccess(AccountRole.RESTAURANT,Set.of("mine"));
  when(accounts.requireRole(user,AccountRole.RESTAURANT)).thenReturn(account);
  var order=new OrderEntity(OrderStatus.PLACED,"buyer","private-phone","private-address","City",140,18,158,Instant.now());
  order.addItem(new OrderItemEntity("mine","My kitchen","dish","Pizza",2,50,100));order.addItem(new OrderItemEntity("other","Other kitchen","secret","Other dish",1,40,40));
  when(repository.findAssignedRestaurantOrders(Set.of("mine"))).thenReturn(List.of(order));
  var result=new RestaurantOrdersController(accounts,repository,service).list(user);
  assertEquals(1,result.size());assertEquals(1,result.get(0).items().size());assertEquals("Pizza",result.get(0).items().get(0).itemName());assertEquals(100,result.get(0).subtotal());assertEquals("",result.get(0).customerPhone());assertEquals("",result.get(0).customerAddress());
 }
 @Test void noAssignmentsReturnsEmptyAndCustomersCannotReadRestaurantOrders(){
  var accounts=mock(AccountService.class);var repository=mock(OrderRepository.class);var controller=new RestaurantOrdersController(accounts,repository,mock(OrderService.class));var user=new AuthenticatedUser("u","u@example.test");
  when(accounts.requireRole(user,AccountRole.RESTAURANT)).thenReturn(new AccountEntity("u","u@example.test",AccountRole.RESTAURANT,AccountStatus.ACTIVE));assertTrue(controller.list(user).isEmpty());verifyNoInteractions(repository);
  when(accounts.requireRole(user,AccountRole.RESTAURANT)).thenThrow(new SecurityException());assertThrows(SecurityException.class,()->controller.list(user));
 }

 @Test void staffCanAdvanceAssignedKitchenOrdersButCannotSkipOrEditMixedOrders(){
  var accounts=mock(AccountService.class);var repository=mock(OrderRepository.class);var service=mock(OrderService.class);var controller=new RestaurantOrdersController(accounts,repository,service);
  var user=new AuthenticatedUser("staff","staff@example.test");var account=new AccountEntity("staff","staff@example.test",AccountRole.RESTAURANT,AccountStatus.ACTIVE);account.assignAccess(AccountRole.RESTAURANT,Set.of("mine"));when(accounts.requireRole(user,AccountRole.RESTAURANT)).thenReturn(account);
  var order=new OrderEntity(OrderStatus.PLACED,"buyer","phone","address","City",50,18,68,Instant.now());order.addItem(new OrderItemEntity("mine","Kitchen","dish","Dish",1,50,50));when(repository.lockById(1L)).thenReturn(Optional.of(order));
  controller.updateStatus(user,1L,OrderStatus.ACCEPTED);verify(service).updateStatus("1",OrderStatus.ACCEPTED);
  assertThrows(IllegalArgumentException.class,()->controller.updateStatus(user,1L,OrderStatus.READY));
  order.setStatus(OrderStatus.ACCEPTED);controller.updateStatus(user,1L,OrderStatus.PREPARING);verify(service).updateStatus("1",OrderStatus.PREPARING);
  order.setStatus(OrderStatus.PREPARING);controller.updateStatus(user,1L,OrderStatus.READY);verify(service).updateStatus("1",OrderStatus.READY);
  assertThrows(IllegalArgumentException.class,()->controller.updateStatus(user,1L,OrderStatus.DELIVERED));
  order.addItem(new OrderItemEntity("other","Other","other","Other",1,50,50));assertThrows(SecurityException.class,()->controller.updateStatus(user,1L,OrderStatus.READY));
  account.assignAccess(AccountRole.RESTAURANT,Set.of());assertThrows(SecurityException.class,()->controller.updateStatus(user,1L,OrderStatus.READY));
 }
}
