package com.deliveryapp.account;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Hibernate's development schema update does not reliably expand existing enum CHECK constraints.
@Component
public class RoleSchemaUpgrade implements ApplicationRunner {
  private final JdbcTemplate jdbc;
  public RoleSchemaUpgrade(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @Override @Transactional
  public void run(ApplicationArguments args) {
    jdbc.execute("ALTER TABLE app_users DROP CONSTRAINT IF EXISTS app_users_role_check");
    jdbc.execute("ALTER TABLE app_users ADD CONSTRAINT app_users_role_check CHECK (role IN ('CUSTOMER', 'ADMIN', 'SUB_ADMIN', 'DELIVERY', 'RESTAURANT'))");
  }
}
