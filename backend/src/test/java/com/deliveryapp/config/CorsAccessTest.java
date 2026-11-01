package com.deliveryapp.config;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorsAccessTest {
  private static final String ORIGIN = "https://kapinda-6x3.pages.dev";

  @Test void preflightSucceedsBeforeAuthenticationWithoutToken() throws Exception {
    var registration = new WebConfig().apiCorsFilter(ORIGIN);
    assertEquals(Ordered.HIGHEST_PRECEDENCE, registration.getOrder());
    var request = new MockHttpServletRequest("OPTIONS", "/api/account/me");
    request.addHeader("Origin", ORIGIN);
    request.addHeader("Access-Control-Request-Method", "GET");
    request.addHeader("Access-Control-Request-Headers", "authorization");
    var response = new MockHttpServletResponse();
    registration.getFilter().doFilter(request, response, (req, res) -> fail("Preflight must finish before authentication"));
    assertEquals(200, response.getStatus());
    assertEquals(ORIGIN, response.getHeader("Access-Control-Allow-Origin"));
    assertTrue(response.getHeader("Access-Control-Allow-Headers").toLowerCase().contains("authorization"));
  }

  @Test void accountRequestStillRequiresAuthenticationAndRetainsCorsHeaders() throws Exception {
    var authentication = new FirebaseAuthenticationFilter("", "");
    var request = new MockHttpServletRequest("GET", "/api/account/me");
    request.addHeader("Origin", ORIGIN);
    var response = new MockHttpServletResponse();
    FilterChain chain = (req, res) -> authentication.doFilter(req, res, (innerReq, innerRes) -> fail("Unauthenticated request must not reach controller"));
    new WebConfig().apiCorsFilter(ORIGIN).getFilter().doFilter(request, response, chain);
    assertEquals(503, response.getStatus()); // Missing Firebase configuration still rejects access.
    assertEquals(ORIGIN, response.getHeader("Access-Control-Allow-Origin"));
  }

  @Test void unknownOriginIsRejected() throws Exception {
    var request = new MockHttpServletRequest("OPTIONS", "/api/account/me");
    request.addHeader("Origin", "https://untrusted.example");
    request.addHeader("Access-Control-Request-Method", "GET");
    var response = new MockHttpServletResponse();
    new WebConfig().apiCorsFilter(ORIGIN).getFilter().doFilter(request, response, (req, res) -> fail("Unknown origin must be rejected"));
    assertEquals(403, response.getStatus());
    assertNull(response.getHeader("Access-Control-Allow-Origin"));
  }
}
