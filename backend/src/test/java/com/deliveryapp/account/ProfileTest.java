package com.deliveryapp.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.validation.Validation;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProfileTest {
  @Test
  void profileUpdatePreservesIdentityEmailAndRole() {
    AccountRepository repository = mock(AccountRepository.class);
    AccountEntity original = new AccountEntity("customer-a", "a@example.test", AccountRole.CUSTOMER, AccountStatus.ACTIVE);
    when(repository.findById("customer-a")).thenReturn(Optional.of(original));
    when(repository.save(original)).thenReturn(original);
    AccountEntity saved = new AccountService(repository, "").updateProfile(
      new AuthenticatedUser("customer-a", "a@example.test"), new UpdateProfileRequest("  Local customer  ", " 01012345678 "));
    assertEquals("Local customer", saved.getDisplayName());
    assertEquals("01012345678", saved.getPhoneNumber());
    assertEquals("customer-a", saved.getUid());
    assertEquals("a@example.test", saved.getEmail());
    assertEquals(AccountRole.CUSTOMER, saved.getRole());
    verify(repository).findById("customer-a");
  }

  @Test
  void profileRejectsBlankNamesAndInvalidPhoneNumbers() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertFalse(validator.validate(new UpdateProfileRequest(" ", "01012345678")).isEmpty());
      assertFalse(validator.validate(new UpdateProfileRequest("Customer", "abc")).isEmpty());
      assertTrue(validator.validate(new UpdateProfileRequest("Customer", "+20 1012345678")).isEmpty());
    }
  }

  @Test
  void inactiveAccountCannotChangeProfile() {
    AccountRepository repository = mock(AccountRepository.class);
    AccountEntity account = new AccountEntity("customer-a", "a@example.test", AccountRole.CUSTOMER, AccountStatus.REJECTED);
    when(repository.findById("customer-a")).thenReturn(Optional.of(account));
    assertThrows(SecurityException.class, () -> new AccountService(repository, "").updateProfile(
      new AuthenticatedUser("customer-a", "a@example.test"), new UpdateProfileRequest("Customer", "01012345678")));
    verify(repository, never()).save(any());
    assertNull(account.getDisplayName());
  }
}
