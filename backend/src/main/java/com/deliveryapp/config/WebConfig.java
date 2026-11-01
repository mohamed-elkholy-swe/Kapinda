package com.deliveryapp.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {
  @Bean
  public FilterRegistrationBean<CorsFilter> apiCorsFilter(
    @Value("${delivery.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,https://kapinda-6x3.pages.dev}") String allowedOrigins
  ) {
    var config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(value -> !value.isEmpty()).toList());
    config.setAllowedMethods(List.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setMaxAge(3600L);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    var registration = new FilterRegistrationBean<>(new CorsFilter(source));
    // Preflight requests carry no login token and must finish before authentication.
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
