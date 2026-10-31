package com.deliveryapp.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from OrderEntity o where o.id = :id")
  java.util.Optional<OrderEntity> lockById(@Param("id") Long id);

  @Query("select o from OrderEntity o where (o.deliveryPersonUid = :uid) or (o.deliveryPersonUid is null and o.status = com.deliveryapp.order.OrderStatus.READY) order by o.createdAt desc")
  List<OrderEntity> findDeliveryTasks(@Param("uid") String uid);
  List<OrderEntity> findAllByCustomerUidOrderByCreatedAtDesc(String customerUid);
  @Query("select distinct o from OrderEntity o join fetch o.items i where i.restaurantId = :restaurantId order by o.createdAt desc")
  List<OrderEntity> findAllForRestaurant(@Param("restaurantId") String restaurantId);
}
