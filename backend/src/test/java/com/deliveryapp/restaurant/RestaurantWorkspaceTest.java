package com.deliveryapp.restaurant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.deliveryapp.account.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class RestaurantWorkspaceTest {
  private final AccountRepository repository = mock(AccountRepository.class);
  private final RestaurantService restaurants = mock(RestaurantService.class);
  private final AccountService accounts = new AccountService(repository, "");
  private final RestaurantWorkspaceController controller = new RestaurantWorkspaceController(accounts, restaurants);
  private final AuthenticatedUser staff = new AuthenticatedUser("staff", "staff@example.test");
  private void setup() {
    var account = new AccountEntity("staff", "staff@example.test", AccountRole.RESTAURANT, AccountStatus.ACTIVE);
    account.assignAccess(AccountRole.RESTAURANT, Set.of("mine"));
    when(repository.findById("staff")).thenReturn(Optional.of(account));
  }
  @Test void deniesEditingAnotherRestaurantEvenWithKnownId() {
    setup();
    var request = new SaveRestaurantRequest("Name", "food", "30 min", 0, "restaurant");
    assertThrows(SecurityException.class, () -> controller.update(staff, "someone-elses", request));
    assertThrows(SecurityException.class, () -> controller.deleteItem(staff, "someone-elses", "dish"));
    assertThrows(SecurityException.class, () -> controller.addCategory(staff, "someone-elses", new SaveMenuCategoryRequest("Pizza")));
    verifyNoInteractions(restaurants);
  }
  @Test void listsOnlyAssignedRestaurantsAndAllowsTheirUpdates() {
    setup();
    var mine = new Restaurant("mine", "My kitchen", "food", "New", "30 min", 0, "restaurant", List.of());
    var other = new Restaurant("other", "Other kitchen", "food", "New", "30 min", 0, "restaurant", List.of());
    when(restaurants.findAll(null, null)).thenReturn(List.of(mine, other));
    assertEquals(List.of(mine), controller.list(staff));
    var request = new SaveRestaurantRequest("Name", "food", "30 min", 0, "restaurant");
    controller.update(staff, "mine", request);
    verify(restaurants).update("mine", request);
  }
  @Test void ordinaryCustomerCannotUseRestaurantWorkspace() {
    when(repository.findById("staff")).thenReturn(Optional.of(new AccountEntity("staff", "staff@example.test", AccountRole.CUSTOMER, AccountStatus.ACTIVE)));
    assertThrows(SecurityException.class, () -> controller.list(staff));
    verifyNoInteractions(restaurants);
  }
  @Test void administratorsCanListAndEditAllRestaurantsWithoutAssignments() {
    var first = new Restaurant("one", "One", "food", "New", "30 min", 0, "restaurant", List.of());
    var second = new Restaurant("two", "Two", "food", "New", "30 min", 0, "restaurant", List.of());
    when(restaurants.findAll(null, null)).thenReturn(List.of(first, second));
    for (var role : List.of(AccountRole.ADMIN, AccountRole.SUB_ADMIN)) {
      when(repository.findById("staff")).thenReturn(Optional.of(new AccountEntity("staff", "staff@example.test", role, AccountStatus.ACTIVE)));
      assertEquals(List.of(first, second), controller.list(staff));
      assertDoesNotThrow(() -> controller.update(staff, "two", new SaveRestaurantRequest("Name", "food", "30 min", 0, "restaurant")));
    }
    verify(restaurants, times(2)).update(eq("two"), any());
  }
}
