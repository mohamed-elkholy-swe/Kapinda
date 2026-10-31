package com.deliveryapp.account;

import com.deliveryapp.restaurant.RestaurantRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountAdministrationService {
  private final AccountService accounts;
  private final AccountRepository repository;
  private final RestaurantRepository restaurants;
  public AccountAdministrationService(AccountService accounts, AccountRepository repository, RestaurantRepository restaurants) {
    this.accounts = accounts; this.repository = repository; this.restaurants = restaurants;
  }
  @Transactional(readOnly = true)
  public List<AccountResponse> search(AuthenticatedUser actor, String query) {
    accounts.requireRole(actor, AccountRole.ADMIN);
    return repository.search(query == null ? "" : query.trim(), PageRequest.of(0, 50)).stream().map(AccountResponse::from).toList();
  }
  @Transactional
  public AccountResponse assign(AuthenticatedUser actor, String uid, AccountRole role, Set<String> restaurantIds) {
    AccountEntity administrator = accounts.requireRole(actor, AccountRole.ADMIN);
    AccountEntity target = repository.findById(uid).orElseThrow(() -> new IllegalArgumentException("User not found. They must sign in once first."));
    if (administrator.getRole() != AccountRole.ADMIN && (role == AccountRole.ADMIN || target.getRole() == AccountRole.ADMIN))
      throw new SecurityException("Only a full administrator can grant or change Administrator access");
    if (actor.uid().equals(uid) && role != administrator.getRole()) throw new IllegalArgumentException("You cannot remove your own administrator access");
    if (role == AccountRole.RESTAURANT) {
      if (restaurantIds.isEmpty()) throw new IllegalArgumentException("Assign at least one restaurant to restaurant staff");
      for (String id : restaurantIds) if (!restaurants.existsById(id)) throw new IllegalArgumentException("An assigned restaurant does not exist");
    } else if (!restaurantIds.isEmpty()) throw new IllegalArgumentException("Restaurant assignments are only available for restaurant staff");
    target.assignAccess(role, restaurantIds);
    return AccountResponse.from(repository.save(target));
  }
}
