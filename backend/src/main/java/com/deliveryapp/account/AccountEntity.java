package com.deliveryapp.account;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
public class AccountEntity {
  @Id
  @Column(name = "firebase_uid")
  private String uid;

  @Column(nullable = false)
  private String email;

  @Column(name = "display_name", length = 100)
  private String displayName;

  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountStatus status;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "account_restaurant_assignments", joinColumns = @JoinColumn(name = "firebase_uid"))
  @Column(name = "restaurant_id", nullable = false)
  private Set<String> restaurantIds = new HashSet<>();

  protected AccountEntity() {}

  public AccountEntity(String uid, String email, AccountRole role, AccountStatus status) {
    this.uid = uid;
    this.email = email;
    this.role = role;
    this.status = status;
  }

  public String getUid() { return uid; }
  public String getEmail() { return email; }
  public String getDisplayName() { return displayName; }
  public String getPhoneNumber() { return phoneNumber; }
  public void updateProfile(String displayName, String phoneNumber) {
    this.displayName = displayName;
    this.phoneNumber = phoneNumber;
  }
  public AccountRole getRole() { return role; }
  public AccountStatus getStatus() { return status; }
  public Set<String> getRestaurantIds() { return restaurantIds; }
  public void assignAccess(AccountRole role, Set<String> restaurantIds) {
    this.role = role;
    this.restaurantIds.clear();
    if (role == AccountRole.RESTAURANT) this.restaurantIds.addAll(restaurantIds);
  }
}
