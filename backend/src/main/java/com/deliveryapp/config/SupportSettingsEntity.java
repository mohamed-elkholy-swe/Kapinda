package com.deliveryapp.config;
import jakarta.persistence.*;
@Entity @Table(name = "support_settings")
public class SupportSettingsEntity {
  @Id private Long id;
  @Column(length = 20) private String phone;
  protected SupportSettingsEntity() {}
  public SupportSettingsEntity(String phone) { this.id = 1L; this.phone = phone; }
  public String getPhone() { return phone; }
}
