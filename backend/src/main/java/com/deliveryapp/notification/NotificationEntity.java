package com.deliveryapp.notification;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="notifications", indexes=@Index(name="notification_recipient_idx",columnList="recipientUid"))
public class NotificationEntity {
  @Id private String id;
  @Column(nullable=false) private String recipientUid;
  @Column(nullable=false) private Long orderId;
  @Column(nullable=false) private String status;
  @Column(nullable=false) private Instant createdAt;
  @Column(name="is_read",nullable=false) private boolean read;
  @Column(nullable=false,columnDefinition="boolean default false") private boolean dismissed;
  public boolean isDismissed(){return dismissed;}
  protected NotificationEntity() {}
  public NotificationEntity(String uid, Long orderId, String status) { this.id=uid+":"+orderId+":"+status; this.recipientUid=uid;this.orderId=orderId;this.status=status;this.createdAt=Instant.now(); }
  public String getId(){return id;} public Long getOrderId(){return orderId;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public boolean isRead(){return read;} public void markRead(){read=true;}
}
