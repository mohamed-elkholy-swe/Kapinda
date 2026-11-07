package com.deliveryapp.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<RestaurantEntity, String> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select r from RestaurantEntity r where r.id = :id")
  Optional<RestaurantEntity> findForRating(@org.springframework.data.repository.query.Param("id") String id);
  Optional<RestaurantEntity> findByOwnerUid(String ownerUid);
  List<RestaurantEntity> findAllByApprovalStatus(RestaurantApprovalStatus status);
}
