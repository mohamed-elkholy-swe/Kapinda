package com.deliveryapp.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

/** Reads local credentials without copying them into application.properties or Windows settings. */
public class LocalDotEnvEnvironment implements EnvironmentPostProcessor, Ordered {
  private static final Set<String> KEYS = Set.of("DATABASE_URL", "DATABASE_USERNAME", "DATABASE_PASSWORD", "FIREBASE_PROJECT_ID", "FIREBASE_CREDENTIALS_PATH", "DELIVERY_ADMIN_EMAILS", "DELIVERY_SUPPORT_PHONE");
  @Override public int getOrder() { return ConfigDataEnvironmentPostProcessor.ORDER + 1; }
  @Override public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    Path cwd = Path.of("").toAbsolutePath().normalize();
    String explicit = environment.getProperty("delivery.env-file", environment.getProperty("DELIVERY_ENV_FILE", ""));
    List<Path> candidates = explicit.isBlank() ? List.of(cwd.resolve("backend/.env"), cwd.resolve(".env")) : List.of(Path.of(explicit).toAbsolutePath());
    Path file = candidates.stream().filter(Files::isRegularFile).findFirst().orElse(null);
    if (file == null) {
      if (!explicit.isBlank()) throw new IllegalStateException("The configured local .env file does not exist");
      return;
    }
    try {
      Map<String, Object> values = read(file);
      // Nonempty explicit overrides keep precedence. Empty IDE environment fields mean "unset".
      Map<String, Object> emptyOverrides = new HashMap<>();
      values.forEach((key, value) -> {
        String existing = environment.getProperty(key);
        if (existing != null && existing.isBlank() && value instanceof String local && !local.isBlank()) emptyOverrides.put(key, value);
      });
      environment.getPropertySources().addLast(new MapPropertySource("kapindaLocalDotEnv", values));
      if (!emptyOverrides.isEmpty()) environment.getPropertySources().addFirst(new MapPropertySource("kapindaEmptyLocalOverrides", emptyOverrides));
    } catch (IOException exception) {
      throw new IllegalStateException("Could not read the local backend .env configuration");
    }
  }
  static Map<String, Object> read(Path file) throws IOException {
    Map<String, Object> result = new HashMap<>();
    for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
      String line = raw.replace("\uFEFF", "").trim();
      if (line.isEmpty() || line.startsWith("#")) continue;
      if (line.startsWith("export ")) line = line.substring(7).trim();
      int separator = line.indexOf('=');
      if (separator < 1) continue;
      String key = line.substring(0, separator).trim();
      String value = line.substring(separator + 1).trim();
      if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) value = value.substring(1, value.length() - 1);
      if (key.equals("EXPO_PUBLIC_FIREBASE_PROJECT_ID") && !result.containsKey("FIREBASE_PROJECT_ID")) result.put("FIREBASE_PROJECT_ID", value);
      if (KEYS.contains(key)) result.put(key, value);
    }
    Object credentials = result.get("FIREBASE_CREDENTIALS_PATH");
    if (credentials instanceof String value && !value.isBlank()) {
      Path path = Path.of(value);
      if (!path.isAbsolute()) {
        Path folder = file.toAbsolutePath().normalize().getParent();
        Path base = folder;
        path = base.resolve(path);
      }
      result.put("FIREBASE_CREDENTIALS_PATH", path.toAbsolutePath().normalize().toString());
    }
    return result;
  }
}
