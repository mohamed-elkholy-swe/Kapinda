package com.deliveryapp.notification;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Component;
@Component
public class FirebasePushSender {
  public static class SendFailure extends RuntimeException {
    final MessagingErrorCode code;
    public SendFailure(MessagingErrorCode code) {super("Push delivery failed");this.code=code;}
  }
  public void send(String token,String tag) {
    // Keep customer and order details off shared lock screens.
    try { FirebaseMessaging.getInstance().send(Message.builder().setToken(token)
      .putData("title","Kapinda · كابيندا")
      .putData("body","لديك تحديث جديد للطلبات · You have an order update")
      .putData("tag",tag)
      .setWebpushConfig(WebpushConfig.builder().putHeader("TTL","3600").build()).build());
    } catch(FirebaseMessagingException e) {throw new SendFailure(e.getMessagingErrorCode());}
  }
}
