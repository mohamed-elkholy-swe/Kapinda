package com.deliveryapp.account;

public enum AccountRole {
  CUSTOMER,
  ADMIN,
  SUB_ADMIN,
  DELIVERY,
  RESTAURANT;

  public boolean isAdministrator() { return this == ADMIN || this == SUB_ADMIN; }

  // Existing ADMIN endpoint guards describe operational capabilities. Granting ADMIN itself
  // is separately restricted to the exact ADMIN role by AccountAdministrationService.
  public boolean permits(AccountRole required) { return this == required || isAdministrator(); }
}
