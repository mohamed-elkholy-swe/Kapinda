package com.deliveryapp.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.deliveryapp.restaurant.RestaurantRepository;
import java.util.*;
import org.junit.jupiter.api.Test;

class SubAdminAccessTest {
  private final AccountRepository repository = mock(AccountRepository.class);
  private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
  private final AccountService accounts = new AccountService(repository, "");
  private final AccountAdministrationService access = new AccountAdministrationService(accounts, repository, restaurants);
  private final AuthenticatedUser sub = new AuthenticatedUser("sub", "sub@example.test");
  private final AuthenticatedUser admin = new AuthenticatedUser("admin", "admin@example.test");
  private final AccountEntity target = new AccountEntity("customer", "customer@example.test", AccountRole.CUSTOMER, AccountStatus.ACTIVE);
  private void setup() {
    when(repository.findById("sub")).thenReturn(Optional.of(new AccountEntity("sub", "sub@example.test", AccountRole.SUB_ADMIN, AccountStatus.ACTIVE)));
    when(repository.findById("admin")).thenReturn(Optional.of(new AccountEntity("admin", "admin@example.test", AccountRole.ADMIN, AccountStatus.ACTIVE)));
    when(repository.findById("customer")).thenReturn(Optional.of(target));
    when(repository.save(target)).thenReturn(target);
  }
  @Test void subAdminCannotPromoteThemselvesOrAnotherAccountToAdmin() {
    setup();
    assertThrows(SecurityException.class, () -> access.assign(sub, "sub", AccountRole.ADMIN, Set.of()));
    assertThrows(SecurityException.class, () -> access.assign(sub, "customer", AccountRole.ADMIN, Set.of()));
    verify(repository, never()).save(any());
    assertEquals(AccountRole.CUSTOMER, target.getRole());
  }
  @Test void subAdminCannotChangeAnExistingFullAdministrator() {
    setup();
    assertThrows(SecurityException.class, () -> access.assign(sub, "admin", AccountRole.CUSTOMER, Set.of()));
    assertThrows(SecurityException.class, () -> access.assign(sub, "admin", AccountRole.SUB_ADMIN, Set.of()));
    verify(repository, never()).save(any());
  }
  @Test void subAdminCanManageAllOtherRolesAndRestaurantAssignments() {
    setup(); when(restaurants.existsById("r1")).thenReturn(true); when(restaurants.existsById("r2")).thenReturn(true);
    assertEquals(AccountRole.DELIVERY, access.assign(sub, "customer", AccountRole.DELIVERY, Set.of()).role());
    assertEquals(Set.of("r1", "r2"), access.assign(sub, "customer", AccountRole.RESTAURANT, Set.of("r1", "r2")).restaurantIds());
    assertEquals(AccountRole.SUB_ADMIN, access.assign(sub, "customer", AccountRole.SUB_ADMIN, Set.of()).role());
    assertTrue(target.getRestaurantIds().isEmpty());
    assertEquals(AccountRole.CUSTOMER, access.assign(sub, "customer", AccountRole.CUSTOMER, Set.of()).role());
  }
  @Test void onlyFullAdminCanGrantFullAdminAndCanAlsoGrantSubAdmin() {
    setup();
    assertEquals(AccountRole.ADMIN, access.assign(admin, "customer", AccountRole.ADMIN, Set.of()).role());
    assertEquals(AccountRole.SUB_ADMIN, access.assign(admin, "customer", AccountRole.SUB_ADMIN, Set.of()).role());
  }
  @Test void bothAdministratorRolesHaveAllOperationalCapabilities() {
    setup();
    for (var actor : List.of(admin, sub)) {
      for (var role : AccountRole.values()) assertNotNull(accounts.requireRole(actor, role));
      assertDoesNotThrow(() -> accounts.requireRestaurant(actor, "not-assigned"));
    }
    assertThrows(SecurityException.class, () -> accounts.requireRole(new AuthenticatedUser("customer", "customer@example.test"), AccountRole.ADMIN));
  }
  @Test void inactiveAdminDoesNotInheritAnyPrivileges() {
    setup();
    when(repository.findById("admin")).thenReturn(Optional.of(new AccountEntity("admin", "admin@example.test", AccountRole.ADMIN, AccountStatus.REJECTED)));
    assertThrows(SecurityException.class, () -> accounts.requireRestaurant(admin, "anywhere"));
    assertThrows(SecurityException.class, () -> accounts.requireRole(admin, AccountRole.DELIVERY));
  }
}
