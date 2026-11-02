package com.deliveryapp.notification;
import com.deliveryapp.account.*;
import com.deliveryapp.order.OrderRepository;
import com.google.firebase.messaging.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.event.*;
@Service
public class PushService {
 public record Notice(String uid,Long orderId,String status,String id) {}
 private final PushDeviceRepository devices;
 private final AccountService accounts;
 private final AccountRepository accountRepository;
 private final OrderRepository orders;
 public PushService(PushDeviceRepository devices,AccountService accounts,AccountRepository accountRepository,OrderRepository orders){this.devices=devices;this.accounts=accounts;this.accountRepository=accountRepository;this.orders=orders;}
 static String key(String token){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 @Transactional public void register(AuthenticatedUser user,String token){accounts.requireActive(user);devices.register(key(token),token,user.uid());}
 @Transactional public void unregister(AuthenticatedUser user,String token){devices.unregister(key(token),user.uid());}
 @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
 @Transactional(propagation=Propagation.REQUIRES_NEW)
 public void send(Notice notice){
  // A push failure must never roll back an order. Recheck access before sending.
  try{
   var account=accountRepository.findById(notice.uid()).orElse(null);
   var order=orders.findById(notice.orderId()).orElse(null);
   if(account==null||order==null||!NotificationService.eligible(account,order,notice.status()))return;
   for(var device:devices.findByUid(notice.uid()))try{
    FirebaseMessaging.getInstance().send(Message.builder().setToken(device.getToken())
     .putData("title","Kapinda · كابيندا").putData("body","You have an order update / لديك تحديث على الطلب").putData("tag",notice.id())
     .putData("page",switch(account.getRole()){case ADMIN,SUB_ADMIN->"admin";case RESTAURANT->"restaurantOrders";case DELIVERY->"delivery";case CUSTOMER->"orders";})
     .putData("deliveries",notice.status().equals("READY")?"available":"mine")
     .setWebpushConfig(WebpushConfig.builder().putHeader("TTL","300").build()).build());
   }catch(FirebaseMessagingException e){
    if(e.getMessagingErrorCode()==MessagingErrorCode.UNREGISTERED)devices.removeInvalid(device.getToken());
    else LoggerFactory.getLogger(PushService.class).warn("Push delivery failed: {}",e.getMessagingErrorCode());
   }
  }catch(Exception e){LoggerFactory.getLogger(PushService.class).warn("Push delivery unavailable ({})",e.getClass().getSimpleName());}
 }
}
