package com.deliveryapp.notification;
import com.deliveryapp.account.*;
import com.deliveryapp.order.OrderRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import java.util.List;
import java.time.Instant;
import com.deliveryapp.order.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
class PushServiceTest {
 @Test void onlyNewInboxRowsPublishPushEvents(){
  var repository=mock(NotificationRepository.class);var accounts=mock(AccountRepository.class);var events=mock(ApplicationEventPublisher.class);
  var account=new AccountEntity("buyer","buyer@example.test",AccountRole.CUSTOMER,AccountStatus.ACTIVE);
  var order=new OrderEntity(OrderStatus.PLACED,"buyer","phone","address","City",50,18,68,Instant.now());
  when(accounts.findAll()).thenReturn(List.of(account));
  when(repository.insertOnce(anyString(),eq("buyer"),nullable(Long.class),eq("PLACED"))).thenReturn(1,0);
  var service=new NotificationService(mock(AccountService.class),mock(OrderRepository.class),repository,accounts);
  ReflectionTestUtils.setField(service,"events",events);
  service.record(order);service.record(order);
  verify(events,times(1)).publishEvent(any(PushService.Notice.class));
 }
 @Test void registrationUsesAuthenticatedOwnerAndRemovalCannotDeleteAnotherOwnersDevice(){
  var devices=mock(PushDeviceRepository.class);var accounts=mock(AccountService.class);
  var service=new PushService(devices,accounts,mock(AccountRepository.class),mock(OrderRepository.class));
  var user=new AuthenticatedUser("me","me@example.test");
  service.register(user,"token");verify(accounts).requireActive(user);verify(devices).register(PushService.key("token"),"token","me");
  service.unregister(user,"token");verify(devices).unregister(PushService.key("token"),"me");
 }
 @Test void blockedRegistrationNeverStoresToken(){
  var devices=mock(PushDeviceRepository.class);var accounts=mock(AccountService.class);var user=new AuthenticatedUser("me","me@example.test");
  when(accounts.requireActive(user)).thenThrow(new SecurityException());
  var service=new PushService(devices,accounts,mock(AccountRepository.class),mock(OrderRepository.class));
  assertThrows(SecurityException.class,()->service.register(user,"token"));verifyNoInteractions(devices);
 }
 @Test void removedAccountReceivesNoPush(){
  var devices=mock(PushDeviceRepository.class);var accounts=mock(AccountRepository.class);var orders=mock(OrderRepository.class);
  when(accounts.findById("deleted")).thenReturn(Optional.empty());when(orders.findById(1L)).thenReturn(Optional.empty());
  new PushService(devices,mock(AccountService.class),accounts,orders).send(new PushService.Notice("deleted",1L,"READY","id"));
  verifyNoInteractions(devices);
 }
}
