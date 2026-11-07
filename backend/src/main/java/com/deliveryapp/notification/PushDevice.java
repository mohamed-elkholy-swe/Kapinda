package com.deliveryapp.notification;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(indexes=@Index(name="push_device_uid_idx",columnList="uid"))
public class PushDevice {
  @Id public String id;
  @Column(nullable=false) public String uid;
  @Column(nullable=false,length=2048) public String token;
  @Column(nullable=false) public Instant updatedAt;
  protected PushDevice() {}
  public PushDevice(String id,String uid,String token) {this.id=id;this.uid=uid;this.token=token;this.updatedAt=Instant.now();}
}
