package com.yooyoung.clotheser.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yooyoung.clotheser.global.entity.BaseResponse;
import com.yooyoung.clotheser.global.jwt.JwtAuthenticationFilter;
import com.yooyoung.clotheser.global.jwt.JwtProvider;
import com.yooyoung.clotheser.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.PrintWriter;
import java.util.List;

import static com.yooyoung.clotheser.global.entity.BaseResponseStatus.*;
import static org.springframework.http.HttpStatus.*;

@Configuration      // 스프링의 기본 설정 정보들의 환경 세팅을 돕는 어노테이션
@EnableWebSecurity  // 스프링 시큐리티를 활성화하는 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CorsConfig corsConfig;


    // 특정 HTTP 요청에 대한 웹 기반 보안 구성
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity             // SecurityFilterChain에서 요청에 접근할 수 있어서 인증, 인가 서비스에 사용
                .httpBasic(HttpBasicConfigurer::disable)    // http basic auth 기반으로 로그인 인증창이 뜨는데, 기본 인증을 이용하지 않으려면 .disable() 추가
                .csrf(AbstractHttpConfigurer::disable)  // api server 이용 시 세션 기반 인증 disable (html tag를 통한 공격 방지)
                .formLogin(AbstractHttpConfigurer::disable) //
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests((authorize) -> authorize     // 각 경로별 권한 처리
                        .requestMatchers("/api/v1/users/signup").permitAll()    // 작성된 경로의 api 요청은 인증 없이 모두 허용
                        .requestMatchers("/api/v1/users/check-nickname/{nickname}").permitAll()
                        .requestMatchers("/api/v1/users/login").permitAll()
                        .requestMatchers("/ws/**").permitAll()  // 웹소켓 엔드포인트 허용
                        .anyRequest().authenticated()   // 지정된 URL 이외의 요청은 인증 필요
                )
                .sessionManagement((session) -> session     // 세션 생성 및 사용 여부에 대한 정책 설정
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt 사용하는 경우 (STATELESS는 인증 정보를 서버에 담지 않음)
                )
                // UserNamePasswordAuthenticationFilter 적용하기 전에 JWTTokenFilter를 적용하라는 뜻
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, refreshTokenRepository), UsernamePasswordAuthenticationFilter.class)
                // 에러 핸들링
                .exceptionHandling(exception -> exception
                        // 인증 예외 처리 (토큰이 없는 경우)
                        .authenticationEntryPoint((request, response, authException) -> {
                            BaseResponse<Object> br = new BaseResponse<>(EMPTY_JWT);
                            ObjectMapper objectMapper = new ObjectMapper();

                            response.setStatus(UNAUTHORIZED.value());
                            String json = objectMapper.writeValueAsString(br);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(json);
                            writer.flush();
                        })
                        // 인가 예외 처리 (권한이 없는 경우)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            BaseResponse<Object> br = new BaseResponse<>(INVALID_USER_JWT);
                            ObjectMapper objectMapper = new ObjectMapper();

                            response.setStatus(FORBIDDEN.value());
                            String json = objectMapper.writeValueAsString(br);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(json);
                            writer.flush();
                        })
                )
                .build();
    }

    // 비밀번호 암호화 (bcrypt 강력 해싱 알고리즘 사용)
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
