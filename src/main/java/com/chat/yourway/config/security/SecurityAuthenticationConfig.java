package com.chat.yourway.config.security;

import com.chat.yourway.security.MyPasswordEncoder;
import com.chat.yourway.security.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@RequiredArgsConstructor
public class SecurityAuthenticationConfig {

  private final MyUserDetailsService myUserDetailsService;
  private final MyPasswordEncoder myPasswordEncoder;

  @Bean
  public AuthenticationProvider authenticationProvider() {
    var authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(myUserDetailsService);
    authProvider.setPasswordEncoder(myPasswordEncoder);
    return authProvider;
  }

  @Bean
  @SneakyThrows
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
    return authConfig.getAuthenticationManager();
  }
}
