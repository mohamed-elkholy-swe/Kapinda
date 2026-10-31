package com.deliveryapp.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
  @NotBlank @Size(max = 100) String displayName,
  @NotBlank @Pattern(regexp = "[+0-9 ()-]{10,20}") String phoneNumber
) {}
