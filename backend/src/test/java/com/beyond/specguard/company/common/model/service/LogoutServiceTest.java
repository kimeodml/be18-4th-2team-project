// file: src/test/java/com/beyond/specguard/company/common/model/service/LogoutServiceTest.java
package com.beyond.specguard.company.common.model.service;

import com.beyond.specguard.common.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class LogoutServiceTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisTokenService redisTokenService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("✅ 정상적인 로그아웃 시 Redis 및 쿠키가 제거된다")
    void logout_success() {
        // given
        String accessToken = "valid-access-token";
        String username = "user@test.com";
        String jti = "random-jti";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        when(jwtUtil.isExpired(accessToken)).thenReturn(false);
        when(jwtUtil.getUsername(accessToken)).thenReturn(username);
        when(jwtUtil.getJti(accessToken)).thenReturn(jti);
        when(jwtUtil.getExpiration(accessToken)).thenReturn(new Date(System.currentTimeMillis() + 10000));

        // when
        logoutService.logout(request, response);

        // then
        verify(redisTokenService).deleteRefreshToken(username);
        verify(redisTokenService).blacklistAccessToken(eq(jti), anyLong());
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("❌ Authorization 헤더가 없으면 JwtException 발생")
    void logout_fail_no_header() {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when & then
        JwtException ex = assertThrows(JwtException.class, () -> logoutService.logout(request, response));
        assertEquals("Authorization 헤더가 유효하지 않습니다.", ex.getMessage());
        verifyNoInteractions(redisTokenService);
    }

    @Test
    @DisplayName("❌ 토큰이 만료되었으면 JwtException 발생")
    void logout_fail_expired_token() {
        // given
        String accessToken = "expired-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        when(jwtUtil.isExpired(accessToken)).thenReturn(true);

        // when & then
        JwtException ex = assertThrows(JwtException.class, () -> logoutService.logout(request, response));
        assertEquals("이미 만료된 Access Token입니다.", ex.getMessage());
        verifyNoInteractions(redisTokenService);
    }
}
