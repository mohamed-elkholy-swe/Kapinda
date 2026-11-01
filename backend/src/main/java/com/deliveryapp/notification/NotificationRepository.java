package com.deliveryapp.notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface NotificationRepository extends JpaRepository<NotificationEntity,String> {
  @org.springframework.data.jpa.repository.Modifying
  @org.springframework.data.jpa.repository.Query(value="insert into notifications (id,recipient_uid,order_id,status,created_at,is_read,dismissed) values (:id,:uid,:orderId,:status,CURRENT_TIMESTAMP,false,false) on conflict (id) do nothing",nativeQuery=true)
  void insertOnce(@org.springframework.data.repository.query.Param("id") String id,@org.springframework.data.repository.query.Param("uid") String uid,@org.springframework.data.repository.query.Param("orderId") Long orderId,@org.springframework.data.repository.query.Param("status") String status);
  @org.springframework.data.jpa.repository.Modifying(clearAutomatically=true,flushAutomatically=true)
  @org.springframework.data.jpa.repository.Query("update NotificationEntity n set n.read=true where n.recipientUid=:uid and n.dismissed=false")
  void markAllRead(@org.springframework.data.repository.query.Param("uid") String uid);
  @org.springframework.data.jpa.repository.Modifying(clearAutomatically=true,flushAutomatically=true)
  @org.springframework.data.jpa.repository.Query("update NotificationEntity n set n.dismissed=true,n.read=true where n.recipientUid=:uid")
  void dismissAll(@org.springframework.data.repository.query.Param("uid") String uid);
  List<NotificationEntity> findByRecipientUidOrderByCreatedAtDesc(String uid);
  Optional<NotificationEntity> findByIdAndRecipientUid(String id,String uid);
}
