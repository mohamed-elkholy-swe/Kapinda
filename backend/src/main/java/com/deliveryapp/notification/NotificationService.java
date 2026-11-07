package com.deliveryapp.notification;

import com.deliveryapp.account.*;
import com.deliveryapp.order.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
  private PushService push;
  @org.springframework.beans.factory.annotation.Autowired
  public void setPush(PushService push) {this.push=push;}
  private final AccountService accounts;
  private final OrderRepository orders;
  private final NotificationRepository notifications;
  private final AccountRepository accountRepository;

  public NotificationService(AccountService accounts, OrderRepository orders, NotificationRepository notifications, AccountRepository accountRepository) {
    this.accounts=accounts; this.orders=orders; this.notifications=notifications; this.accountRepository=accountRepository;
  }

  public static boolean visible(AccountEntity account, OrderEntity order) {
    if(account.getStatus()!=AccountStatus.ACTIVE) return false;
    return switch(account.getRole()) {
      case ADMIN, SUB_ADMIN -> true;
      case CUSTOMER -> account.getUid().equals(order.getCustomerUid());
      case RESTAURANT -> order.getItems().stream().anyMatch(i->account.getRestaurantIds().contains(i.getRestaurantId()));
      case DELIVERY -> account.getUid().equals(order.getDeliveryPersonUid()) || (order.getDeliveryPersonUid()==null && order.getStatus()==OrderStatus.READY);
    };
  }

  public static boolean eligible(AccountEntity account, OrderEntity order, String status) {
    if(!visible(account,order)) return false;
    // A courier's assignment does not grant access to earlier kitchen notifications.
    if(account.getRole()==AccountRole.DELIVERY) return Set.of("READY","OUT_FOR_DELIVERY","DELIVERED","CANCELLED").contains(status);
    return true;
  }

  public record Item(String id, Long orderId, String status, java.time.Instant createdAt, boolean read, String target) {}

  @Transactional(readOnly=true)
  public List<Item> list(AuthenticatedUser user) {
    var account=accounts.requireActive(user);
    var accessible=new HashMap<Long,OrderEntity>();
    orders.findAll().stream().filter(o->visible(account,o)).forEach(o->accessible.put(o.getId(),o));
    return notifications.findByRecipientUidOrderByCreatedAtDesc(user.uid()).stream()
      .filter(n->!n.isDismissed() && accessible.containsKey(n.getOrderId()) && eligible(account,accessible.get(n.getOrderId()),n.getStatus()))
      .limit(100).map(n->new Item(n.getId(),n.getOrderId(),n.getStatus(),n.getCreatedAt(),n.isRead(),target(account))).toList();
  }

  private String target(AccountEntity account) {
    return switch(account.getRole()) { case ADMIN,SUB_ADMIN -> "admin"; case RESTAURANT -> "restaurantOrders"; case DELIVERY -> "delivery"; case CUSTOMER -> "orders"; };
  }

  @Transactional
  public void record(OrderEntity order) {
    for(var account:accountRepository.findAll()) if(eligible(account,order,order.getStatus().name())) {
      String id=account.getUid()+":"+order.getId()+":"+order.getStatus().name();
      int inserted=notifications.insertOnce(id,account.getUid(),order.getId(),order.getStatus().name());
      if(inserted>0 && push!=null) push.enqueue(id,account.getUid(),order.getId(),order.getStatus().name());
    }
  }

  @Transactional
  public void read(AuthenticatedUser user,String id) {
    var account=accounts.requireActive(user);
    var notification=notifications.findByIdAndRecipientUid(id,user.uid()).orElseThrow(()->new SecurityException("Notification not available"));
    var order=orders.findById(notification.getOrderId()).orElseThrow(()->new SecurityException("Notification not available"));
    if(notification.isDismissed() || !eligible(account,order,notification.getStatus())) throw new SecurityException("Notification not available");
    notification.markRead();
  }

  @Transactional
  public void readAll(AuthenticatedUser user) { accounts.requireActive(user); notifications.markAllRead(user.uid()); }

  @Transactional
  public void removeAll(AuthenticatedUser user) { accounts.requireActive(user); notifications.dismissAll(user.uid()); }
}
