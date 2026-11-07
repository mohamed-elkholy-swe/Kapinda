package com.deliveryapp.notification;

import com.deliveryapp.account.*;
import com.deliveryapp.order.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class NotificationActorTest {
  private AccountEntity account(String uid,AccountRole role) {
    return new AccountEntity(uid,uid+"@example.test",role,AccountStatus.ACTIVE);
  }
  @Test void actorIsExcludedFromHistoryAndPushWhileOtherEligibleAccountsAreNotified() {
    for (String actor : List.of("admin","other-admin","sub-admin","restaurant","courier","buyer")) {
      var repository=mock(NotificationRepository.class);var accounts=mock(AccountRepository.class);var push=mock(PushService.class);
      var service=new NotificationService(mock(AccountService.class),mock(OrderRepository.class),repository,accounts);service.setPush(push);
      var staff=account("restaurant",AccountRole.RESTAURANT);staff.assignAccess(AccountRole.RESTAURANT,Set.of("kitchen"));
      var recipients=List.of(account("admin",AccountRole.ADMIN),account("other-admin",AccountRole.ADMIN),
        account("sub-admin",AccountRole.SUB_ADMIN),staff,account("courier",AccountRole.DELIVERY),account("buyer",AccountRole.CUSTOMER));
      var all=new ArrayList<>(recipients);all.add(account("unrelated",AccountRole.CUSTOMER));
      when(accounts.findAll()).thenReturn(all);when(repository.insertOnce(anyString(),anyString(),any(),anyString())).thenReturn(1);
      var order=new OrderEntity(OrderStatus.OUT_FOR_DELIVERY,"buyer","phone","address","City",50,18,68,Instant.now());
      order.addItem(new OrderItemEntity("kitchen","Kitchen","dish","Dish",1,50,50));order.assignDeliveryPerson("courier");
      service.record(order,actor);
      for (var recipient : recipients) {
        var mode=recipient.getUid().equals(actor)?never():times(1);
        verify(repository,mode).insertOnce(anyString(),eq(recipient.getUid()),any(),eq("OUT_FOR_DELIVERY"));
        verify(push,mode).enqueue(anyString(),eq(recipient.getUid()),any(),eq("OUT_FOR_DELIVERY"));
      }
      verify(repository,never()).insertOnce(anyString(),eq("unrelated"),any(),anyString());verifyNoMoreInteractions(push);
    }
  }
  @Test void newCustomerOrderNotifiesStaffButNotItsCreator() {
    var repository=mock(NotificationRepository.class);var accounts=mock(AccountRepository.class);
    var service=new NotificationService(mock(AccountService.class),mock(OrderRepository.class),repository,accounts);
    when(accounts.findAll()).thenReturn(List.of(account("admin",AccountRole.ADMIN),account("buyer",AccountRole.CUSTOMER)));
    service.record(new OrderEntity(OrderStatus.PLACED,"buyer","phone","address","City",50,18,68,Instant.now()),"buyer");
    verify(repository).insertOnce(anyString(),eq("admin"),any(),eq("PLACED"));
    verify(repository,never()).insertOnce(anyString(),eq("buyer"),any(),anyString());
  }
  @Test void guestOrdersStillNotifyEligibleStaff() {
    var repository=mock(NotificationRepository.class);var accounts=mock(AccountRepository.class);
    var service=new NotificationService(mock(AccountService.class),mock(OrderRepository.class),repository,accounts);
    when(accounts.findAll()).thenReturn(List.of(account("admin",AccountRole.ADMIN)));
    service.record(new OrderEntity(OrderStatus.PLACED,null,"phone","address","City",50,18,68,Instant.now()),null);
    verify(repository).insertOnce(anyString(),eq("admin"),any(),eq("PLACED"));
  }
}
