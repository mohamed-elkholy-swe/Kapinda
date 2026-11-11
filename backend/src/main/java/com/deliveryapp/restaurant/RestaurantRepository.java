package com.deliveryapp.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<RestaurantEntity, String> {
  @org.springframework.data.jpa.repository.Query("""
    select new com.deliveryapp.restaurant.RestaurantSummary(
      r.id, r.name, r.category, r.rating, r.deliveryTime, r.minimumOrder, r.art,
      r.ratingCount, r.details.photoUrl, r.details.orderingTermsVerified)
    from RestaurantEntity r
    where (r.active = true or r.active is null)
      and (:search = '' or locate(:search, lower(r.name)) > 0
        or locate(:search, lower(r.category)) > 0
        or exists (select m.id from MenuItemEntity m where m.restaurant = r
          and (m.active = true or m.active is null) and locate(:search, lower(m.name)) > 0))
    order by r.name
    """)
  List<RestaurantSummary> findPublicSummaries(@org.springframework.data.repository.query.Param("search") String search);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select r from RestaurantEntity r where r.id = :id")
  Optional<RestaurantEntity> findForRating(@org.springframework.data.repository.query.Param("id") String id);
  Optional<RestaurantEntity> findByOwnerUid(String ownerUid);
  List<RestaurantEntity> findAllByApprovalStatus(RestaurantApprovalStatus status);
}
