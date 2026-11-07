package com.deliveryapp.restaurant;

import com.deliveryapp.account.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActivationTest {
  @Test void inactiveRecordsStayManageableAndReactivationPreservesItemState() {
    var repository = mock(RestaurantRepository.class);
    var items = mock(MenuItemRepository.class);
    var service = new RestaurantService(repository, items);
    var restaurant = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    var item = new MenuItemEntity("i", "Pizza", "", 50);
    restaurant.addMenuItem(item);
    when(repository.findAll()).thenReturn(List.of(restaurant));
    when(repository.findById("r")).thenReturn(Optional.of(restaurant));
    when(items.findById("i")).thenReturn(Optional.of(item));
    assertTrue(service.findById("r").active());
    assertFalse(service.setMenuItemActive("r", "i", false).active());
    assertEquals(1, service.findById("r").menu().size());
    assertTrue(service.findPublicById("r").menu().isEmpty());
    assertTrue(service.findPublic(null, "Pizza").isEmpty());
    service.setActive("r", false);
    assertEquals(1, service.findAll(null, null).size());
    assertTrue(service.findPublic(null, null).isEmpty());
    assertThrows(RestaurantNotFoundException.class, () -> service.findPublicById("r"));
    service.setActive("r", true);
    assertTrue(service.findPublicById("r").menu().isEmpty());
    service.setMenuItemActive("r", "i", true);
    assertEquals(1, service.findPublic(null, "Pizza").get(0).menu().size());
    assertThrows(MenuItemNotFoundException.class, () -> service.setMenuItemActive("other", "i", false));
  }

  @Test void legacyNullFlagsRemainActiveAndRequestsRequireExplicitState() throws Exception {
    var restaurant = new RestaurantEntity("r", "Kitchen", "food", "New", "30 min", 0, "restaurant");
    var item = new MenuItemEntity("i", "Pizza", "", 50);
    for (Object entity : List.of(restaurant, item)) {
      var field = entity.getClass().getDeclaredField("active");
      field.setAccessible(true); field.set(entity, null);
    }
    assertTrue(restaurant.isActive()); assertTrue(item.isActive());
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      assertFalse(factory.getValidator().validate(new SetActiveRequest(null)).isEmpty());
      assertTrue(factory.getValidator().validate(new SetActiveRequest(false)).isEmpty());
    }
  }

  @Test void activationEndpointsRequireAdminOrAssignedRestaurant() {
    var accounts = mock(AccountService.class);
    var service = mock(RestaurantService.class);
    var user = new AuthenticatedUser("u", "u@example.test");
    var workspace = new RestaurantWorkspaceController(accounts, service);
    workspace.setActive(user, "mine", new SetActiveRequest(false));
    workspace.setMenuItemActive(user, "mine", "i", new SetActiveRequest(true));
    verify(accounts, times(2)).requireRestaurant(user, "mine");
    verify(service).setActive("mine", false);
    verify(service).setMenuItemActive("mine", "i", true);
    clearInvocations(service);
    doThrow(new SecurityException()).when(accounts).requireRestaurant(user, "other");
    assertThrows(SecurityException.class, () -> workspace.setActive(user, "other", new SetActiveRequest(false)));
    assertThrows(SecurityException.class, () -> workspace.setMenuItemActive(user, "other", "i", new SetActiveRequest(false)));
    when(accounts.requireRole(user, AccountRole.ADMIN)).thenThrow(new SecurityException());
    var admin = new AdminRestaurantController(service, accounts);
    assertThrows(SecurityException.class, () -> admin.setActive(user, "mine", new SetActiveRequest(false)));
    assertThrows(SecurityException.class, () -> admin.setMenuItemActive(user, "mine", "i", new SetActiveRequest(false)));
    verifyNoInteractions(service);
  }
}
