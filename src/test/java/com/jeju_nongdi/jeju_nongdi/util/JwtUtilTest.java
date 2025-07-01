package com.jeju_nongdi.jeju_nongdi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET = "testJejuNongdiSecretKey2025ForSecureAuthentication123456789012345";
    private final Long EXPIRATION = 3600000L; // 1시간
    private final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
        jwtUtil.init(); // @PostConstruct 메서드 수동 호출
    }

    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void generateToken() {
        // when
        String token = jwtUtil.generateToken(EMAIL);

        // then
        assertThat(token).isNotEmpty();

        // 토큰 검증
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 테스트")
    void getEmailFromToken() {
        // given
        String token = jwtUtil.generateToken(EMAIL);

        // when
        String extractedEmail = jwtUtil.getEmailFromToken(token);

        // then
        assertThat(extractedEmail).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("토큰 유효성 검사 성공 테스트")
    void validateTokenSuccess() {
        // given
        String token = jwtUtil.generateToken(EMAIL);

        // when
        boolean isValid = jwtUtil.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰 유효성 검사 실패 테스트")
    void validateExpiredToken() {
        // given
        // 수동으로 만료된 토큰 생성
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(2, ChronoUnit.HOURS);

        String expiredToken = Jwts.builder()
                .subject(EMAIL)
                .issuedAt(Date.from(oneHourAgo))
                .expiration(Date.from(oneHourAgo.plus(1, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();

        // when
        boolean isValid = jwtUtil.validateToken(expiredToken);
        boolean isExpired = jwtUtil.isTokenExpired(expiredToken);

        // then
        assertThat(isValid).isFalse();
        assertThat(isExpired).isTrue();
    }
}
