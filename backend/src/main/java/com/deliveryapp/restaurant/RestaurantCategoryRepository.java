package com.deliveryapp.restaurant;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategoryEntity, String> {
  Optional<RestaurantCategoryEntity> findByNameArKeyOrNameEnKey(String arabicKey, String englishKey);
}
