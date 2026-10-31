package com.deliveryapp.config;

import com.deliveryapp.account.AuthenticatedUser;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);
  private final boolean configured;

  public FirebaseAuthenticationFilter(
    @Value("${firebase.project-id:}") String projectId,
    @Value("${firebase.credentials-path:}") String credentialsPath
  ) {
    boolean ready = false;
    if (!projectId.isBlank()) {
      try (InputStream credentialsStream = credentialsStream(credentialsPath)) {
        if (FirebaseApp.getApps().isEmpty()) {
          FirebaseApp.initializeApp(FirebaseOptions.builder()
            .setCredentials(credentialsStream == null
              ? GoogleCredentials.getApplicationDefault()
              : GoogleCredentials.fromStream(credentialsStream))
            .setProjectId(projectId)
            .build());
        }
        ready = true;
      } catch (IOException exception) {
        // Credential parser messages may contain private data; log only a fixed diagnostic.
        logger.error("Firebase Admin initialization failed ({}). Check that FIREBASE_CREDENTIALS_PATH points to a readable, valid service-account JSON file. Protected API requests will return 503.", exception.getClass().getSimpleName());
      }
    } else {
      logger.error("Firebase Admin is disabled: FIREBASE_PROJECT_ID is missing. Load the backend environment before starting Java. Protected API requests will return 503.");
    }
    configured = ready;
    if (configured) logger.info("Firebase Admin initialized. Protected API authentication is ready.");
  }

  private InputStream credentialsStream(String credentialsPath) throws IOException {
    if (credentialsPath == null || credentialsPath.isBlank()) return null;
    Path path = Path.of(credentialsPath).toAbsolutePath().normalize();
    if (!Files.isRegularFile(path)) throw new IOException("Firebase credentials file not found: " + path);
    return new FileInputStream(path.toFile());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {
    if (!requiresAuthentication(request.getRequestURI())) {
      chain.doFilter(request, response);
      return;
    }
    String authorization = request.getHeader("Authorization");
    if (!configured) {
      response.sendError(503, "Firebase Admin is not configured");
      return;
    }
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      response.sendError(401, "Missing Firebase token");
      return;
    }
    FirebaseToken token;
    try {
      token = FirebaseAuth.getInstance().verifyIdToken(authorization.substring(7));
    } catch (Exception exception) {
      response.sendError(401, "Invalid Firebase token");
      return;
    }
      request.setAttribute("authenticatedUser", new AuthenticatedUser(token.getUid(), token.getEmail() == null ? "" : token.getEmail()));
      chain.doFilter(request, response);
  }

  private boolean requiresAuthentication(String uri) {
    return uri.startsWith("/api/account")
      || uri.startsWith("/api/admin")
      || uri.startsWith("/api/delivery")
      || uri.startsWith("/api/restaurant-workspace")
      || uri.startsWith("/api/orders");
  }
}
