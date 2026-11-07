package com.deliveryapp.notification;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(indexes=@Index(name="push_job_due_idx",columnList="nextAttempt"))
public class PushJob {
  @Id @Column(length=512) public String id;
  public String deviceId;
  public String uid;
  public Long orderId;
  public String status;
  public int attempts;
  public Instant nextAttempt;
  protected PushJob() {}
  public PushJob(String notificationId,PushDevice device,Long orderId,String status) {
    this.id=notificationId+":"+device.id;this.deviceId=device.id;this.uid=device.uid;
    this.orderId=orderId;this.status=status;this.nextAttempt=Instant.now();
  }
}
