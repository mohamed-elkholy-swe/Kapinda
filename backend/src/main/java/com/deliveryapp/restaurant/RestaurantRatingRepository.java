package com.deliveryapp.restaurant;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRatingRepository extends JpaRepository<RestaurantRatingEntity, String> {
  Optional<RestaurantRatingEntity> findByRestaurantIdAndCustomerUid(String restaurantId, String customerUid);
  long countByRestaurantId(String restaurantId);
  @Query("select r from RestaurantRatingEntity r where r.restaurant.id = :id and r.feedback is not null and trim(r.feedback) <> '' order by r.updatedAt desc, r.id desc")
  org.springframework.data.domain.Slice<RestaurantRatingEntity> publicFeedback(@Param("id") String id, org.springframework.data.domain.Pageable pageable);
  @Query("select avg(r.stars) from RestaurantRatingEntity r where r.restaurant.id = :id")
  Double averageForRestaurant(@Param("id") String id);
}
