package com.deliveryapp.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.deliveryapp.restaurant.RestaurantRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AccountAccessTest {
  private final AccountRepository repository = mock(AccountRepository.class);
  private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
  private final AccountService accounts = new AccountService(repository, "");
  private final AccountAdministrationService service = new AccountAdministrationService(accounts, repository, restaurants);
  private final AuthenticatedUser admin = new AuthenticatedUser("admin", "admin@example.test");
  private AccountEntity setup() {
    when(repository.findById("admin")).thenReturn(Optional.of(new AccountEntity("admin", "admin@example.test", AccountRole.ADMIN, AccountStatus.ACTIVE)));
    var target = new AccountEntity("staff", "staff@example.test", AccountRole.CUSTOMER, AccountStatus.ACTIVE);
    when(repository.findById("staff")).thenReturn(Optional.of(target));
    when(repository.save(target)).thenReturn(target);
    return target;
  }
  @Test void assignsMultipleRestaurantsAndClearsThemWhenRoleChanges() {
    var target = setup();
    when(restaurants.existsById("one")).thenReturn(true);
    when(restaurants.existsById("two")).thenReturn(true);
    var result = service.assign(admin, "staff", AccountRole.RESTAURANT, Set.of("one", "two"));
    assertEquals(Set.of("one", "two"), result.restaurantIds());
    assertEquals(AccountRole.RESTAURANT, target.getRole());
    service.assign(admin, "staff", AccountRole.DELIVERY, Set.of());
    assertTrue(target.getRestaurantIds().isEmpty());
    assertEquals(AccountRole.DELIVERY, target.getRole());
  }
  @Test void rejectsMissingRestaurantAndEmptyRestaurantAssignments() {
    var target = setup();
    assertThrows(IllegalArgumentException.class, () -> service.assign(admin, "staff", AccountRole.RESTAURANT, Set.of("missing")));
    assertThrows(IllegalArgumentException.class, () -> service.assign(admin, "staff", AccountRole.RESTAURANT, Set.of()));
    assertEquals(AccountRole.CUSTOMER, target.getRole());
    verify(repository, never()).save(any());
  }
  @Test void customerCannotGrantThemselvesAdminOrSearchUsers() {
    setup(); var customer = new AuthenticatedUser("staff", "staff@example.test");
    assertThrows(SecurityException.class, () -> service.assign(customer, "staff", AccountRole.ADMIN, Set.of()));
    assertThrows(SecurityException.class, () -> service.search(customer, ""));
    verify(repository, never()).save(any());
    verify(repository, never()).search(anyString(), any());
  }
  @Test void administratorCannotDemoteTheirOwnAccount() {
    setup();
    assertThrows(IllegalArgumentException.class, () -> service.assign(admin, "admin", AccountRole.CUSTOMER, Set.of()));
    verify(repository, never()).save(any());
  }
  @Test void allActiveRolesRetainCustomerCapabilities() {
    for (AccountRole role : AccountRole.values()) {
      var account = new AccountEntity("staff", "staff@example.test", role, AccountStatus.ACTIVE);
      when(repository.findById("staff")).thenReturn(Optional.of(account));
      assertEquals(account, accounts.requireActive(new AuthenticatedUser("staff", "staff@example.test")));
    }
  }
}
