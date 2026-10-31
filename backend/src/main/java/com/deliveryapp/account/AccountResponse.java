package com.deliveryapp.account;

public record AccountResponse(String uid, String email, AccountRole role, AccountStatus status, String displayName, String phoneNumber, java.util.Set<String> restaurantIds) {
  public static AccountResponse from(AccountEntity account) {
    return new AccountResponse(account.getUid(), account.getEmail(), account.getRole(), account.getStatus(), account.getDisplayName(), account.getPhoneNumber(), java.util.Set.copyOf(account.getRestaurantIds()));
  }
}
