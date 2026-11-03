package com.deliveryapp.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryAreaRepository extends JpaRepository<DeliveryArea, Integer> {
  List<DeliveryArea> findAllByOrderByIdAsc();
  Optional<DeliveryArea> findByName(String name);
  boolean existsByNameAndIdNot(String name, int id);

  @Modifying
  @Query(value = "insert into delivery_areas (id, name, fee) values (:id, :name, :fee) on conflict do nothing", nativeQuery = true)
  void seed(@Param("id") int id, @Param("name") String name, @Param("fee") int fee);
}
