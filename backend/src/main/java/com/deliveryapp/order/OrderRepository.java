package com.deliveryapp.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from OrderEntity o where o.id = :id")
  java.util.Optional<OrderEntity> lockById(@Param("id") Long id);

  @Query("select o from OrderEntity o where (o.deliveryPersonUid = :uid) or (o.deliveryPersonUid is null and o.status = com.deliveryapp.order.OrderStatus.READY) order by o.createdAt desc")
  List<OrderEntity> findDeliveryTasks(@Param("uid") String uid);
  @Query("select distinct o from OrderEntity o join o.items i where i.restaurantId in :ids order by o.createdAt desc")
  List<OrderEntity> findAssignedRestaurantOrders(@Param("ids") java.util.Set<String> ids);
  List<OrderEntity> findAllByCustomerUidOrderByCreatedAtDesc(String customerUid);
  Slice<OrderEntity> findByCustomerUidAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(String customerUid, Instant start, Instant end, Pageable page);
  Slice<OrderEntity> findByDeliveryPersonUidAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(String deliveryPersonUid, Instant start, Instant end, Pageable page);
  @Query("select o from OrderEntity o where o.createdAt >= :start and o.createdAt < :end and exists (select i from OrderItemEntity i where i.order = o and i.restaurantId in :ids) order by o.createdAt desc, o.id desc")
  Slice<OrderEntity> findRecentRestaurantOrders(@Param("ids") java.util.Set<String> ids, @Param("start") Instant start,
    @Param("end") Instant end, Pageable page);
  @Query("select distinct o from OrderEntity o join fetch o.items i where i.restaurantId = :restaurantId order by o.createdAt desc")
  List<OrderEntity> findAllForRestaurant(@Param("restaurantId") String restaurantId);
}
