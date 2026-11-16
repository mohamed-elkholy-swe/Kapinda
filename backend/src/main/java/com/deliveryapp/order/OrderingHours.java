package com.deliveryapp.order;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;

public final class OrderingHours {
  private OrderingHours() {}
  public static final String NOTICE = "التطبيق لا يقبل الطلبات يومياً من ٤:٠٠ صباحاً إلى ٤:٠٠ مساءً بتوقيت القاهرة. / The application does not accept orders daily from 4:00 AM to 4:00 PM Cairo time.";

  public static boolean isOpen(Clock clock) {
    var time = LocalTime.now(clock.withZone(ZoneId.of("Africa/Cairo")));
    return time.isBefore(LocalTime.of(4, 0)) || !time.isBefore(LocalTime.of(16, 0));
  }
}
