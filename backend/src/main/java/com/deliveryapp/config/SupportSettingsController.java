package com.deliveryapp.config;
import com.deliveryapp.account.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;
@RestController
public class SupportSettingsController {
  private final SupportSettingsRepository repository;
  private final AccountService accounts;
  private final String defaultPhone;
  public SupportSettingsController(SupportSettingsRepository repository, AccountService accounts, @org.springframework.beans.factory.annotation.Value("${delivery.support-phone:}") String defaultPhone) { this.repository = repository; this.accounts = accounts; this.defaultPhone = defaultPhone; }
  public record Settings(@NotNull @Pattern(regexp = "^$|[+0-9 ()-]{10,20}") String supportPhone) {}
  @GetMapping("/api/settings")
  public Settings settings() { return new Settings(repository.findById(1L).map(SupportSettingsEntity::getPhone).orElse(defaultPhone)); }
  @PutMapping("/api/admin/settings")
  public Settings update(@RequestAttribute AuthenticatedUser authenticatedUser, @Valid @RequestBody Settings settings) {
    accounts.requireRole(authenticatedUser, AccountRole.ADMIN);
    repository.save(new SupportSettingsEntity(settings.supportPhone().trim()));
    return settings();
  }
}
