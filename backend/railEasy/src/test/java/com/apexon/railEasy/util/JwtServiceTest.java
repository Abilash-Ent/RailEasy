package com.apexon.railEasy.util;

import com.apexon.railEasy.config.JwtProperties;
import com.apexon.railEasy.constants.Role;
import com.apexon.railEasy.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-that-is-long-enough-0123456789");
        properties.setExpirationMs(3_600_000L);
        properties.setIssuer("railEasy");
        jwtService = new JwtService(properties);
    }

    private User user() {
        return User.builder().id(5L).email("user@raileasy.com").role(Role.USER).build();
    }

    @Test
    void generateToken_thenExtractClaims_roundTrips() {
        String token = jwtService.generateToken(user());

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@raileasy.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
        assertThat(jwtService.parseClaims(token).get("userId", Integer.class)).isEqualTo(5);
    }

    @Test
    void isTokenValid_returnsFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        JwtProperties expired = new JwtProperties();
        expired.setSecret("test-secret-key-that-is-long-enough-0123456789");
        expired.setExpirationMs(-1_000L); // already expired
        expired.setIssuer("railEasy");
        String token = new JwtService(expired).generateToken(user());

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseWhenIssuerMismatch() {
        JwtProperties other = new JwtProperties();
        other.setSecret("test-secret-key-that-is-long-enough-0123456789");
        other.setExpirationMs(3_600_000L);
        other.setIssuer("someone-else");
        String token = new JwtService(other).generateToken(user());

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void getExpirationSeconds_convertsMillisToSeconds() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3_600L);
    }
}
