package com.deliveryapp.notification;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface PushDeviceRepository extends JpaRepository<PushDevice,String> {
 List<PushDevice> findByUid(String uid);
 @Modifying @Query(value="insert into push_devices (id,token,uid,updated_at) values (:id,:token,:uid,CURRENT_TIMESTAMP) on conflict (id) do update set uid=:uid, updated_at=CURRENT_TIMESTAMP",nativeQuery=true)
 void register(@Param("id") String id,@Param("token") String token,@Param("uid") String uid);
 @Modifying @Query("delete from PushDevice d where d.id=:id and d.uid=:uid")
 void unregister(@Param("id") String id,@Param("uid") String uid);
 @Modifying @Query("delete from PushDevice d where d.token=:token")
 void removeInvalid(@Param("token") String token);
}
