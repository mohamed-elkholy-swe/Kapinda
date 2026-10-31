package com.deliveryapp.account;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
  private final AccountRepository repository;
  private final String[] adminEmails;

  public AccountService(AccountRepository repository, @Value("${delivery.admin-emails:}") String adminEmails) {
    this.repository = repository;
    this.adminEmails = Arrays.stream(adminEmails.split(",")).map(String::trim).filter(value -> !value.isBlank()).toArray(String[]::new);
  }

  @Transactional
  public AccountEntity getOrCreate(AuthenticatedUser user) {
    return repository.findById(user.uid()).orElseGet(() -> {
      boolean admin = Arrays.stream(adminEmails).anyMatch(email -> email.equalsIgnoreCase(user.email()));
      return repository.save(new AccountEntity(user.uid(), user.email(), admin ? AccountRole.ADMIN : AccountRole.CUSTOMER, AccountStatus.ACTIVE));
    });
  }

  @Transactional(readOnly = true)
  public AccountEntity requireRole(AuthenticatedUser user, AccountRole role) {
    AccountEntity account = repository.findById(user.uid()).orElseThrow(() -> new SecurityException("Account not registered"));
    if (!account.getRole().permits(role) || account.getStatus() != AccountStatus.ACTIVE) throw new SecurityException("Access denied");
    return account;
  }

  @Transactional
  public AccountEntity updateProfile(AuthenticatedUser user, UpdateProfileRequest request) {
    AccountEntity account = getOrCreate(user);
    if (account.getStatus() != AccountStatus.ACTIVE) throw new SecurityException("Access denied");
    account.updateProfile(request.displayName().trim(), request.phoneNumber().trim());
    return repository.save(account);
  }

  @Transactional(readOnly = true)
  public AccountEntity requireActive(AuthenticatedUser user) {
    AccountEntity account = repository.findById(user.uid()).orElseThrow(() -> new SecurityException("Account not registered"));
    if (account.getStatus() != AccountStatus.ACTIVE) throw new SecurityException("Access denied");
    return account;
  }

  @Transactional(readOnly = true)
  public java.util.Set<String> assignedRestaurantIds(AuthenticatedUser user) {
    return java.util.Set.copyOf(requireRole(user, AccountRole.RESTAURANT).getRestaurantIds());
  }

  @Transactional(readOnly = true)
  public void requireRestaurant(AuthenticatedUser user, String restaurantId) {
    AccountEntity account = requireRole(user, AccountRole.RESTAURANT);
    if (!account.getRole().isAdministrator() && !account.getRestaurantIds().contains(restaurantId))
      throw new SecurityException("This restaurant is not assigned to your account");
  }

  public AccountRepository repository() { return repository; }
}
