package com.deliveryapp.notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PushDeviceRepository extends JpaRepository<PushDevice,String> {
  List<PushDevice> findByUid(String uid);
}
