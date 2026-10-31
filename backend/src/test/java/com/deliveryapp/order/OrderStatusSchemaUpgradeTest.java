package com.deliveryapp.order;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

class OrderStatusSchemaUpgradeTest {
  @Test void replacesLegacyConstraintWithEverySupportedStatusIncludingReady() {
    var jdbc = mock(JdbcTemplate.class);
    var upgrade = new OrderStatusSchemaUpgrade(jdbc);
    upgrade.run(null);
    var calls = inOrder(jdbc);
    calls.verify(jdbc).execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check");
    var sql = ArgumentCaptor.forClass(String.class);
    calls.verify(jdbc).execute(sql.capture());
    assertTrue(sql.getValue().startsWith("ALTER TABLE orders ADD CONSTRAINT orders_status_check CHECK (status IN ("));
    for (var status : OrderStatus.values()) assertTrue(sql.getValue().contains("'" + status.name() + "'"));
    assertTrue(sql.getValue().contains("'READY'"));
    verifyNoMoreInteractions(jdbc);
    assertDoesNotThrow(() -> upgrade.run(null));
  }
}
