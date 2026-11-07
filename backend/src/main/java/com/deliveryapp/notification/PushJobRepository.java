package com.deliveryapp.notification;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PushJobRepository extends JpaRepository<PushJob,String> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.QueryHints(@jakarta.persistence.QueryHint(name="jakarta.persistence.lock.timeout",value="-2"))
  List<PushJob> findTop50ByNextAttemptBeforeOrderByNextAttemptAsc(Instant now);
}
