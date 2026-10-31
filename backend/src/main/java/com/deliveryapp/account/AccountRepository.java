package com.deliveryapp.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {
  @org.springframework.data.jpa.repository.Query("select a from AccountEntity a where lower(a.email) like lower(concat('%', :search, '%')) or lower(coalesce(a.displayName, '')) like lower(concat('%', :search, '%')) or coalesce(a.phoneNumber, '') like concat('%', :search, '%') or a.uid = :search order by a.email")
  java.util.List<AccountEntity> search(@org.springframework.data.repository.query.Param("search") String search, org.springframework.data.domain.Pageable pageable);
}
