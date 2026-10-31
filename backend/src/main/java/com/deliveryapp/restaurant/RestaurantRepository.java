package com.deliveryapp.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<RestaurantEntity, String> {
  Optional<RestaurantEntity> findByOwnerUid(String ownerUid);
  List<RestaurantEntity> findAllByApprovalStatus(RestaurantApprovalStatus status);
}
