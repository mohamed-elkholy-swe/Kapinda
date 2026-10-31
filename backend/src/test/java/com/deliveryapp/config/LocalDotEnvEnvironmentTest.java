package com.deliveryapp.config;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.boot.SpringApplication;

class LocalDotEnvEnvironmentTest {
  @TempDir Path temp;
  @Test void preservesQuotedPasswordsAndWindowsPathsWithoutImportingBrowserSettings() throws Exception {
    Path file = temp.resolve(".env");
    Files.writeString(file, "DATABASE_PASSWORD=\"test#value=with\\backslash\"\nEXPO_PUBLIC_FIREBASE_PROJECT_ID=test-project\nEXPO_PUBLIC_FIREBASE_API_KEY=not-needed\nFIREBASE_CREDENTIALS_PATH='" + temp.resolve("service-account.json") + "'\n");
    var values = LocalDotEnvEnvironment.read(file);
    assertEquals("test#value=with\\backslash", values.get("DATABASE_PASSWORD"));
    assertEquals("test-project", values.get("FIREBASE_PROJECT_ID"));
    assertEquals(temp.resolve("service-account.json").toString(), values.get("FIREBASE_CREDENTIALS_PATH"));
    assertFalse(values.containsKey("EXPO_PUBLIC_FIREBASE_API_KEY"));
  }
  @Test void credentialPathsResolveFromEnvDirectory() throws Exception {
    Path backend = Files.createDirectory(temp.resolve("backend"));
    Path file = backend.resolve(".env");
    Files.writeString(file, "FIREBASE_CREDENTIALS_PATH=./config/key.json\n");
    assertEquals(temp.resolve("backend/config/key.json").toString(), LocalDotEnvEnvironment.read(file).get("FIREBASE_CREDENTIALS_PATH"));
  }
  @Test void startupLoadsDotEnvAndPreservesExplicitOverrides() throws Exception {
    Path file = temp.resolve(".env");
    Files.writeString(file, "DATABASE_USERNAME=local-user\nDATABASE_PASSWORD=test-secret\n");
    var environment = new MockEnvironment().withProperty("delivery.env-file", file.toString()).withProperty("DATABASE_USERNAME", "override-user");
    new LocalDotEnvEnvironment().postProcessEnvironment(environment, new SpringApplication());
    assertEquals("override-user", environment.getProperty("DATABASE_USERNAME"));
    assertEquals("test-secret", environment.getProperty("DATABASE_PASSWORD"));
  }
  @Test void emptyIdeEnvironmentFieldsDoNotHideTheDotEnvValues() throws Exception {
    Path file = temp.resolve(".env");
    Files.writeString(file, "DATABASE_URL=jdbc:postgresql://localhost/example\nEXPO_PUBLIC_FIREBASE_PROJECT_ID=test-project\n");
    var environment = new MockEnvironment().withProperty("delivery.env-file", file.toString()).withProperty("DATABASE_URL", "").withProperty("FIREBASE_PROJECT_ID", "");
    new LocalDotEnvEnvironment().postProcessEnvironment(environment, new SpringApplication());
    assertEquals("jdbc:postgresql://localhost/example", environment.getProperty("DATABASE_URL"));
    assertEquals("test-project", environment.getProperty("FIREBASE_PROJECT_ID"));
  }
}
