package com.yooyoung.clotheser.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yooyoung.clotheser.global.entity.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtProvider jwtProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, java.io.IOException {
        // Request Header에서 JWT 추출
        String accessToken = jwtProvider.resolveToken((HttpServletRequest) request);

        try {
            if (accessToken != null && jwtProvider.validateToken(accessToken)) {
                // 로그아웃한 액세스 토큰인 경우 401 에러
                jwtProvider.checkLogoutToken(accessToken);
                // 리프레시 토큰인 경우 로그인 막기 (403)
                jwtProvider.checkIsRefreshToken(accessToken);
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
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
