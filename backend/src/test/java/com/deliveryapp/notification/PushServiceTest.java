package com.deliveryapp.notification;
import com.deliveryapp.account.*;
import com.deliveryapp.order.*;
import com.google.firebase.messaging.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PushServiceTest {
  final PushDeviceRepository devices=mock(PushDeviceRepository.class);
  final PushJobRepository jobs=mock(PushJobRepository.class);
  final AccountService accounts=mock(AccountService.class);
  final AccountRepository accountRepository=mock(AccountRepository.class);
  final OrderRepository orders=mock(OrderRepository.class);
  final FirebasePushSender sender=mock(FirebasePushSender.class);
  final PushService service=new PushService(devices,jobs,accounts,accountRepository,orders,sender);
  final PushDevice device=new PushDevice(PushService.deviceId("token"),"buyer","token");
  final PushJob job=new PushJob("notice",device,1L,"READY");
  void ready() {
    when(jobs.findTop50ByNextAttemptBeforeOrderByNextAttemptAsc(any())).thenReturn(List.of(job));
    when(devices.findById(device.id)).thenReturn(Optional.of(device));
    when(accountRepository.findById("buyer")).thenReturn(Optional.of(new AccountEntity("buyer","b@example.test",AccountRole.CUSTOMER,AccountStatus.ACTIVE)));
    when(orders.findById(1L)).thenReturn(Optional.of(new OrderEntity(OrderStatus.READY,"buyer","phone","address","City",50,18,68,Instant.now())));
  }
  @Test void successfulSendRemovesOnlyDeliveredJob() throws Exception {
    ready();service.deliver();verify(sender).send("token",job.id);verify(jobs).delete(job);
  }
  @Test void accountSwitchInvalidatesPreviouslyQueuedMessages() {
    ready();device.uid="someone-else";service.deliver();verifyNoInteractions(sender);verify(jobs).delete(job);
  }
  @Test void revokedAccessCancelsQueuedDelivery() {
    ready();when(accountRepository.findById("buyer")).thenReturn(Optional.of(new AccountEntity("buyer","b@example.test",AccountRole.RESTAURANT,AccountStatus.ACTIVE)));
    service.deliver();verifyNoInteractions(sender);verify(jobs).delete(job);
  }
  @Test void invalidFirebaseTokenIsRemoved() throws Exception {
    ready();doThrow(new FirebasePushSender.SendFailure(MessagingErrorCode.UNREGISTERED)).when(sender).send(anyString(),anyString());
    service.deliver();verify(devices).delete(device);verify(jobs).delete(job);
  }
  @Test void temporaryFailuresBackOffAndEventuallyStop() throws Exception {
    ready();doThrow(new IllegalStateException()).when(sender).send(anyString(),anyString());
    service.deliver();assertEquals(1,job.attempts);assertTrue(job.nextAttempt.isAfter(Instant.now()));verify(jobs).save(job);
    job.attempts=7;service.deliver();verify(jobs).delete(job);
  }
  @Test void cannotUnregisterSomeoneElsesDevice() {
    when(devices.findById(device.id)).thenReturn(Optional.of(device));
    service.unregister(new AuthenticatedUser("other","other@example.test"),"token");verify(devices,never()).delete(any());
  }
  @Test void inactiveAccountsCannotRegister() {
    var user=new AuthenticatedUser("buyer","b@example.test");when(accounts.requireActive(user)).thenThrow(new SecurityException());
    assertThrows(SecurityException.class,()->service.register(user,"token"));verify(devices,never()).save(any());
  }
  @Test void onlyNewNotificationsCreatePushJobs() {
    var notifications=mock(NotificationRepository.class);
    var notificationService=new NotificationService(accounts,orders,notifications,accountRepository);
    var push=mock(PushService.class);notificationService.setPush(push);
    when(accountRepository.findAll()).thenReturn(List.of(new AccountEntity("buyer","b@example.test",AccountRole.CUSTOMER,AccountStatus.ACTIVE)));
    var order=new OrderEntity(OrderStatus.READY,"buyer","phone","address","City",50,18,68,Instant.now());
    notificationService.record(order,null);verifyNoInteractions(push);
    when(notifications.insertOnce(anyString(),anyString(),any(),anyString())).thenReturn(1);
    notificationService.record(order,null);verify(push).enqueue(anyString(),eq("buyer"),any(),eq("READY"));
  }
}
