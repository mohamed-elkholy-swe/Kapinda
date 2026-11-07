package com.deliveryapp.notification;

import com.deliveryapp.account.*;
import com.deliveryapp.order.*;
import com.google.firebase.messaging.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class PushService {
  private final PushDeviceRepository devices;
  private final PushJobRepository jobs;
  private final AccountService accounts;
  private final AccountRepository accountRepository;
  private final OrderRepository orders;
  private final FirebasePushSender sender;
  private static final org.slf4j.Logger logger=org.slf4j.LoggerFactory.getLogger(PushService.class);
  public PushService(PushDeviceRepository devices,PushJobRepository jobs,AccountService accounts,AccountRepository accountRepository,OrderRepository orders,FirebasePushSender sender) {
    this.devices=devices;this.jobs=jobs;this.accounts=accounts;this.accountRepository=accountRepository;this.orders=orders;
    this.sender=sender;
  }
  static String deviceId(String token) {
    try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}
    catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
  }
  @Transactional public void register(AuthenticatedUser user,String token) {
    accounts.requireActive(user);
    devices.save(new PushDevice(deviceId(token),user.uid(),token));
  }
  @Transactional public void unregister(AuthenticatedUser user,String token) {
    devices.findById(deviceId(token)).filter(d->d.uid.equals(user.uid())).ifPresent(devices::delete);
  }
  public void enqueue(String notificationId,String uid,Long orderId,String status) {
    for(var device:devices.findByUid(uid)) jobs.save(new PushJob(notificationId,device,orderId,status));
  }
  @Scheduled(fixedDelay=10000)
  @Transactional
  public void deliver() {
    for(var job:jobs.findTop50ByNextAttemptBeforeOrderByNextAttemptAsc(Instant.now())) {
      var device=devices.findById(job.deviceId).orElse(null);
      var account=accountRepository.findById(job.uid).orElse(null);
      var order=orders.findById(job.orderId).orElse(null);
      // Recheck access and device ownership after role, assignment, or login changes.
      if(device==null || !device.uid.equals(job.uid) || account==null || order==null
          || !NotificationService.eligible(account,order,job.status)
          || !order.getStatus().name().equals(job.status)
          || device.updatedAt.isBefore(Instant.now().minusSeconds(60L*60*24*60))) {jobs.delete(job);continue;}
      try {
        sender.send(device.token,job.id);
        jobs.delete(job);
      } catch(FirebasePushSender.SendFailure e) {
        if(e.code==MessagingErrorCode.UNREGISTERED) {devices.delete(device);jobs.delete(job);}
        else {logger.warn("Push delivery failed with code {}; queued for retry",e.code);retry(job);}
      } catch(IllegalStateException e) {logger.warn("Push delivery unavailable; check Firebase Admin configuration");retry(job);}
    }
  }
  private void retry(PushJob job) {
    if(++job.attempts>=8) jobs.delete(job);
    else {job.nextAttempt=Instant.now().plusSeconds(Math.min(3600,30L << job.attempts));jobs.save(job);}
  }
}
