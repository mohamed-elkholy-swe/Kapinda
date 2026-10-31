package com.deliveryapp.order;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Hibernate schema update does not expand existing enum CHECK constraints. */
@Component
public class OrderStatusSchemaUpgrade implements ApplicationRunner {
  private final JdbcTemplate jdbc;

  public OrderStatusSchemaUpgrade(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    // Derive values from the enum so future statuses stay aligned with the database.
    String statuses = Arrays.stream(OrderStatus.values())
      .map(status -> "'" + status.name() + "'")
      .collect(Collectors.joining(", "));
    jdbc.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check");
    jdbc.execute("ALTER TABLE orders ADD CONSTRAINT orders_status_check CHECK (status IN (" + statuses + "))");
  }
}
