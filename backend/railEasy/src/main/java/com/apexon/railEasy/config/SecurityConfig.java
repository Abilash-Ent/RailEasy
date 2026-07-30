package com.apexon.railEasy.config;

import com.apexon.railEasy.constants.AppConstants;
import com.apexon.railEasy.constants.Role;
import com.apexon.railEasy.security.JwtAccessDeniedHandler;
import com.apexon.railEasy.security.JwtAuthenticationEntryPoint;
import com.apexon.railEasy.security.JwtSecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

/**
 * Reactive security configuration wiring JWT authentication, BCrypt encoding,
 * CORS and endpoint authorization rules.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            AppConstants.AUTH_BASE + "/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/h2-console/**"
    };

    private final JwtSecurityContextRepository securityContextRepository;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtSecurityContextRepository securityContextRepository,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler,
                          CorsConfigurationSource corsConfigurationSource) {
        this.securityContextRepository = securityContextRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(securityContextRepository)
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        // Schedule search & seat availability are public; management is admin-only.
                        .pathMatchers(HttpMethod.GET, AppConstants.SCHEDULE_BASE + "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, AppConstants.SCHEDULE_BASE + "/**").hasRole(Role.ADMIN.name())
                        .pathMatchers(HttpMethod.PUT, AppConstants.SCHEDULE_BASE + "/**").hasRole(Role.ADMIN.name())
                        .pathMatchers(HttpMethod.DELETE, AppConstants.SCHEDULE_BASE + "/**").hasRole(Role.ADMIN.name())
                        .pathMatchers(HttpMethod.GET, AppConstants.TRAIN_BASE + "/**").authenticated()
                        .pathMatchers(HttpMethod.POST, AppConstants.TRAIN_BASE + "/**").hasRole(Role.ADMIN.name())
                        .pathMatchers(HttpMethod.PUT, AppConstants.TRAIN_BASE + "/**").hasRole(Role.ADMIN.name())
                        .pathMatchers(HttpMethod.DELETE, AppConstants.TRAIN_BASE + "/**").hasRole(Role.ADMIN.name())
                        .pathMatchers(AppConstants.BOOKING_BASE + "/**")
                        .hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .anyExchange().authenticated())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}



