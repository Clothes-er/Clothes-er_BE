package com.yooyoung.clotheser.global.jwt;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.user.domain.RefreshToken;
import com.yooyoung.clotheser.user.dto.TokenResponse;
import com.yooyoung.clotheser.user.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    SecretKey key;

    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostConstruct
    protected void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성
    public TokenResponse createToken(Long userId) {
        Claims claims = Jwts.claims().subject(String.valueOf(userId)).build();
        long now = (new Date()).getTime();

        // 토큰 유효 시간
        long accessTokenExp = 60 * 60 * 24 * 1000L;     // 1일
        long refreshTokenExp = accessTokenExp * 14;    // 2주 (14일)

        String accessToken = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenExp))
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshTokenExp))
                .signWith(key)
                .compact();

        return TokenResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 헤더에서 토큰 받아오기
    public String getHeaderToken(HttpServletRequest request, String type) {
        return type.equals("Authorization") ? request.getHeader("Authorization") : request.getHeader("Authorization-refresh");
    }

    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader("Authorization", accessToken);
    }
    public void setHeaderRefreshToken(HttpServletResponse response, String refreshToken) {
        response.setHeader("Authorization-refresh", refreshToken);
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼냄 (권한 확인)
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(token).toString());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰으로부터 userId 획득
    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject());
    }

    // 토큰 검증
    public boolean validateToken(String token) throws BaseException {
        // Bearer 검증
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }

        token = token.substring(7); // "Bearer " 제거
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            // 토큰 유효 시 true 반환
            return true;
        }
        // 토큰 만료 시 예외 처리
        catch (JwtException | IllegalArgumentException e) {
            throw new BaseException(EXPIRED_JWT_TOKEN, BAD_REQUEST);
        }

    }

    // Refresh Token 검증
    public boolean validateRefreshToken(String token) throws BaseException {
        if (!validateToken(token)) {
            return false;
        }

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        return refreshToken.isPresent() && token.equals(refreshToken.get().getToken());
    }

}
