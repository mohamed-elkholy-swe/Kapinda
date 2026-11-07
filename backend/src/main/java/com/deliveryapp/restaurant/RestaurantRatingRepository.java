package com.deliveryapp.restaurant;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRatingRepository extends JpaRepository<RestaurantRatingEntity, String> {
  Optional<RestaurantRatingEntity> findByRestaurantIdAndCustomerUid(String restaurantId, String customerUid);
  long countByRestaurantId(String restaurantId);
  @Query("select avg(r.stars) from RestaurantRatingEntity r where r.restaurant.id = :id")
  Double averageForRestaurant(@Param("id") String id);
}
