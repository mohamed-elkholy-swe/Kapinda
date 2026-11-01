package com.deliveryapp.notification;
import com.deliveryapp.account.*;
import com.deliveryapp.order.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
class NotificationAccessTest {
 private AccountEntity account(String uid,AccountRole role){return new AccountEntity(uid,uid+"@example.test",role,AccountStatus.ACTIVE);}
 @Test void routesOnlyToOwnerAssignedRestaurantManagementAndEligibleCouriers(){
  var order=new OrderEntity(OrderStatus.PLACED,"buyer","phone","address","City",50,18,68,Instant.now());order.addItem(new OrderItemEntity("mine","Kitchen","dish","Dish",1,50,50));
  var restaurant=account("staff",AccountRole.RESTAURANT);restaurant.assignAccess(AccountRole.RESTAURANT,Set.of("mine"));
  assertTrue(NotificationService.visible(restaurant,order));assertFalse(NotificationService.visible(account("other",AccountRole.RESTAURANT),order));assertTrue(NotificationService.visible(account("buyer",AccountRole.CUSTOMER),order));assertFalse(NotificationService.visible(account("stranger",AccountRole.CUSTOMER),order));
  for(var role:List.of(AccountRole.ADMIN,AccountRole.SUB_ADMIN))assertTrue(NotificationService.visible(account("admin",role),order));
  var courier=account("courier",AccountRole.DELIVERY);assertFalse(NotificationService.visible(courier,order));order.setStatus(OrderStatus.READY);assertTrue(NotificationService.visible(courier,order));order.assignDeliveryPerson("other");assertFalse(NotificationService.visible(courier,order));order.assignDeliveryPerson("courier");order.setStatus(OrderStatus.OUT_FOR_DELIVERY);assertTrue(NotificationService.visible(courier,order));
 }
 @Test void cannotMarkAnotherUsersNotificationRead(){
  var accounts=mock(AccountService.class);var repository=mock(NotificationRepository.class);var service=new NotificationService(accounts,mock(OrderRepository.class),repository,mock(AccountRepository.class));var user=new AuthenticatedUser("me","me@example.test");when(repository.findByIdAndRecipientUid("someone-elses","me")).thenReturn(Optional.empty());
  assertThrows(SecurityException.class,()->service.read(user,"someone-elses"));verify(repository).findByIdAndRecipientUid("someone-elses","me");
 }
 @Test void revokedRestaurantAccessHidesOldNotifications(){
  var accounts=mock(AccountService.class);var orders=mock(OrderRepository.class);var repository=mock(NotificationRepository.class);var user=new AuthenticatedUser("staff","staff@example.test");when(accounts.requireActive(user)).thenReturn(account("staff",AccountRole.RESTAURANT));when(orders.findAll()).thenReturn(List.of());when(repository.findByRecipientUidOrderByCreatedAtDesc("staff")).thenReturn(List.of(new NotificationEntity("staff",1L,"PLACED")));
  assertTrue(new NotificationService(accounts,orders,repository,mock(AccountRepository.class)).list(user).isEmpty());
 }

 @Test void staffDoNotReceivePersonalOrderNotificationsOutsideTheirOperationalScope(){
  var order=new OrderEntity(OrderStatus.PLACED,"staff","phone","address","City",50,18,68,Instant.now());order.addItem(new OrderItemEntity("other","Other kitchen","dish","Dish",1,50,50));
  var restaurant=account("staff",AccountRole.RESTAURANT);restaurant.assignAccess(AccountRole.RESTAURANT,Set.of("mine"));assertFalse(NotificationService.visible(restaurant,order));
  assertFalse(NotificationService.visible(account("staff",AccountRole.DELIVERY),order));
 }
 @Test void couriersCannotReadKitchenStagesEvenAfterAssignment(){
  var order=new OrderEntity(OrderStatus.OUT_FOR_DELIVERY,"buyer","phone","address","City",50,18,68,Instant.now());order.assignDeliveryPerson("courier");var courier=account("courier",AccountRole.DELIVERY);
  for(var status:List.of("PLACED","ACCEPTED","PREPARING"))assertFalse(NotificationService.eligible(courier,order,status));
  assertTrue(NotificationService.eligible(courier,order,"OUT_FOR_DELIVERY"));
 }
 @Test void bulkActionsOnlyUseAuthenticatedRecipientAndReadingNeverCreatesNotifications(){
  var accounts=mock(AccountService.class);var orders=mock(OrderRepository.class);var repository=mock(NotificationRepository.class);var user=new AuthenticatedUser("me","me@example.test");var service=new NotificationService(accounts,orders,repository,mock(AccountRepository.class));
  when(accounts.requireActive(user)).thenReturn(account("me",AccountRole.CUSTOMER));when(orders.findAll()).thenReturn(List.of());when(repository.findByRecipientUidOrderByCreatedAtDesc("me")).thenReturn(List.of());
  service.list(user);verify(repository,never()).insertOnce(anyString(),anyString(),anyLong(),anyString());
  service.readAll(user);verify(repository).markAllRead("me");service.removeAll(user);verify(repository).dismissAll("me");verify(accounts,times(3)).requireActive(user);
 }
 @Test void blockedUsersNeverReceiveNotifications(){
  var order=new OrderEntity(OrderStatus.READY,"buyer","phone","address","City",50,18,68,Instant.now());
  var inactive=new AccountEntity("admin","admin@example.test",AccountRole.ADMIN,AccountStatus.REJECTED);
  assertFalse(NotificationService.visible(inactive,order));
 }
}
