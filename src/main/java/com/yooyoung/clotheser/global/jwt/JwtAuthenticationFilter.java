package com.yooyoung.clotheser.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yooyoung.clotheser.global.entity.*;
import com.yooyoung.clotheser.user.domain.RefreshToken;
import com.yooyoung.clotheser.user.dto.response.TokenResponse;
import com.yooyoung.clotheser.user.repository.RefreshTokenRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, java.io.IOException {
        // Request Header에서 JWT 추출
        String accessToken = jwtProvider.getHeaderToken((HttpServletRequest) request, "Authorization");
        String refreshToken = jwtProvider.getHeaderToken((HttpServletRequest) request, "Authorization-refresh");

        try {
            if (accessToken != null) {
                // 1. Access 유효한 경우
                if (jwtProvider.validateToken(accessToken)) {
                    accessToken = accessToken.split(" ")[1].trim();
                    Authentication authentication = jwtProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                else if (refreshToken != null) {
                    boolean isRefreshToken = jwtProvider.validateRefreshToken(refreshToken);
                    refreshToken = refreshToken.split(" ")[1].trim();
                    // 2. Access 만료 & Refresh 유효
                    if (isRefreshToken) {
                        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                                .orElseThrow(() -> new BaseException(INVALID_JWT_TOKEN, NOT_FOUND));

                        Long userId = rt.getUserId();
                        TokenResponse tokenResponse = jwtProvider.createToken(userId);
                        String newAccessToken = tokenResponse.getAccessToken();

                        jwtProvider.setHeaderAccessToken((HttpServletResponse) response, newAccessToken);

                        Authentication authentication = jwtProvider.getAuthentication(newAccessToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                    // 3. Access, Refresh 둘 다 만료
                    else {
                        throw new BaseException(EXPIRED_JWT_TOKEN, BAD_REQUEST);
                    }
                }
            }
        }
        catch (BaseException e) {
            jwtExceptionHandler((HttpServletResponse) response, e.getStatus(), e.getHttpStatus());
            return; // 예외가 발생하면 필터 체인을 중단
        }
        chain.doFilter(request, response);
    }

    // JWT 관련 예외 처리
    public void jwtExceptionHandler(HttpServletResponse response, BaseResponseStatus status, HttpStatus httpStatus) {
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String json = new ObjectMapper().writeValueAsString(new BaseResponse<>(status));
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
