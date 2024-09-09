package com.yooyoung.clotheser.global.jwt;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.util.RedisUtil;
import com.yooyoung.clotheser.user.dto.response.TokenResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    SecretKey key;

    private final RedisUtil redisUtil;

    private final UserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성
    public TokenResponse createToken(Long userId, String role) {
        Claims claims = Jwts.claims().subject(String.valueOf(userId)).build();

        long now = (new Date()).getTime();

        // 토큰 유효 시간
        long accessTokenExp = 60 * 60 * 24 * 1000L;     // 1일
        long refreshTokenExp = accessTokenExp * 14;    // 2주 (14일)

        String accessToken = Jwts.builder()
                .claims(claims)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenExp))
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .claims(claims)
                .claim("role", role)
                .claim("isRefreshToken", true)
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshTokenExp))
                .signWith(key)
                .compact();

        // Redis에 refreshToken 저장 (key: userId, value: 리프레시 토큰)
        redisUtil.setDataExpire("userId: " + userId, refreshToken, refreshTokenExp);

        return TokenResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Request Header에서 JWT 토큰 정보 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼냄 (권한 확인)
    public Authentication getAuthentication(String token) throws BaseException {
        // JWT에서 사용자 역할 추출
        String role = this.getRole(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(token).toString());

        // 역할을 기반으로 권한을 설정
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        return new UsernamePasswordAuthenticationToken(userDetails, "", Collections.singletonList(authority));
    }

    // 토큰으로부터 userId 획득
    public Long getUserId(String token) {
        String userId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
        return Long.parseLong(userId == null ? "-1" : userId);
    }

    // 토큰으로부터 사용자 역할 획득
    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload().get("role", String.class);
    }

    // 토큰 검증
    public boolean validateToken(String token) throws BaseException {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            // 토큰 유효 시 true 반환
            return true;
        }
        // 토큰이 만료된 경우
        catch (ExpiredJwtException e) {
            throw new BaseException(EXPIRED_JWT, UNAUTHORIZED);
        }
        // 토큰이 잘못된 경우
        catch (JwtException | IllegalArgumentException e) {
            throw new BaseException(EMPTY_JWT_CLAIMS, BAD_REQUEST);
        }
    }

    // 로그아웃한 토큰인지 확인 (Redis에 logout 존재)
    public void checkLogoutToken(String token) throws BaseException {
        String logoutValue = redisUtil.getData(token);
        if (logoutValue != null && logoutValue.equals("logout")) {
            throw new BaseException(LOGOUT_JWT, UNAUTHORIZED);
        }
    }

    // 헤더에 있는 게 리프레시 토큰인지 확인
    public void checkIsRefreshToken(String token) throws BaseException {
        Boolean isRefreshToken = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("isRefreshToken", Boolean.class);
        if (isRefreshToken != null && isRefreshToken) {
            throw new BaseException(FORBIDDEN_LOGIN_REFRESH_TOKEN, FORBIDDEN);
        }
    }

    // JWT 토큰의 남은 유효 시간 조회
    public Long getTokenExpirationTime(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        long now = new Date().getTime();
        return expiration.getTime() - now;
    }

    // 로그아웃
    public void logout(Long userId, String refreshToken, HttpServletRequest request) throws BaseException {

        // 리프레시 토큰의 유효성 확인
        validateToken(refreshToken);

        // Redis에서 리프레시 토큰 존재 확인
        if (!redisUtil.getData("userId: " + userId).equals(refreshToken)) {
            throw new BaseException(INVALID_JWT, BAD_REQUEST);
        }

        // Redis에 액세스 토큰을 블랙 리스트로 저장 (key: 액세스 토큰, value: logout)
        String accessToken = resolveToken(request);
        redisUtil.setDataExpire(accessToken, "logout", getTokenExpirationTime(accessToken) / 1000);
        // Redis에서 리프레시 토큰 삭제
        redisUtil.deleteData("userId: " + userId);
    }

    // 토큰 재발급
    public TokenResponse reissueToken(String refreshToken) throws BaseException {

        // 리프레시 토큰의 유효성 확인
        validateToken(refreshToken);

        // 리프레시 토큰으로 유저 불러오기
        Long userId = this.getUserId(refreshToken);
        String role = this.getRole(refreshToken);

        // Redis에서 리프레시 토큰 존재 확인
        if (!redisUtil.getData("userId: " + userId).equals(refreshToken)) {
            throw new BaseException(INVALID_JWT, BAD_REQUEST);
        }

        // Redis에서 리프레시 토큰 삭제
        redisUtil.deleteData("userId: " + userId);

        return createToken(userId, role);
    }

}
