package com.deliveryapp.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "delivery_areas")
public class DeliveryArea {
  @Id
  private int id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private int fee;

  protected DeliveryArea() {}

  public int getId() { return id; }
  public String getName() { return name; }
  public int getFee() { return fee; }

  void update(String name, int fee) {
    this.name = name;
    this.fee = fee;
  }
}
