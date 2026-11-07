package com.deliveryapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class DeliveryBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(DeliveryBackendApplication.class, args);
  }
}
