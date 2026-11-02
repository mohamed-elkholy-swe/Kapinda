package com.deliveryapp.notification;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="push_devices")
public class PushDevice {
 @Id @Column(length=64) private String id;
 @Column(nullable=false,length=4096) private String token;
 @Column(nullable=false) private String uid;
 @Column(nullable=false) private Instant updatedAt;
 protected PushDevice() {}
 public String getToken(){return token;}
}
