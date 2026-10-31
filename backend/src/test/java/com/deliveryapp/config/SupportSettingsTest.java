package com.deliveryapp.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.deliveryapp.account.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SupportSettingsTest {
  @Test void initialPhoneIsUsedUntilAnAdminSavesAnOverride() {
    var repository = mock(SupportSettingsRepository.class);
    var controller = new SupportSettingsController(repository, mock(AccountService.class), "01280193490");
    when(repository.findById(1L)).thenReturn(Optional.empty());
    assertEquals("01280193490", controller.settings().supportPhone());
    when(repository.findById(1L)).thenReturn(Optional.of(new SupportSettingsEntity("01012345678")));
    assertEquals("01012345678", controller.settings().supportPhone());
    when(repository.findById(1L)).thenReturn(Optional.of(new SupportSettingsEntity("")));
    assertEquals("", controller.settings().supportPhone());
  }
  @Test void ordinaryAccountCannotChangeThePublicSupportPhone() {
    var repository = mock(SupportSettingsRepository.class);
    var accounts = mock(AccountService.class);
    var actor = new AuthenticatedUser("customer", "customer@example.test");
    doThrow(new SecurityException("Access denied")).when(accounts).requireRole(actor, AccountRole.ADMIN);
    var controller = new SupportSettingsController(repository, accounts, "01280193490");
    assertThrows(SecurityException.class, () -> controller.update(actor, new SupportSettingsController.Settings("01012345678")));
    verifyNoInteractions(repository);
  }
}
