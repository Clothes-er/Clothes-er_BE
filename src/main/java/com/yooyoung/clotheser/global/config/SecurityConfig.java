package com.yooyoung.clotheser.global.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity  // 스프링 시큐리티를 활성화하는 어노테이션
@Configuration      // 스프링의 기본 설정 정보들의 환경 세팅을 돕는 어노테이션
// @EnableGlobalMethodSecurity(prePostEnabled = true)  // Controller에서 특정 페이지에 권한이 있는 유저만 접근을 허용할 경우
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity             // SecurityFilterChain에서 요청에 접근할 수 있어서 인증, 인가 서비스에 사용
                .httpBasic(HttpBasicConfigurer::disable)    // http basic auth 기반으로 로그인 인증창이 뜨는데, 기본 인증을 이용하지 않으려면 .disable() 추가
                .csrf(AbstractHttpConfigurer::disable)  // csrf, api server 이용 시 .disable (html tag를 통한 공격)
                .cors(Customizer.withDefaults())	 //  다른 도메인의 리소스에 대해 접근이 허용되는지 체크
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/v1/users/signup", "/api/v1/users/login").permitAll() // 안에 작성된 경로의 api 요청은 인증 없이 모두 허용
                        .anyRequest().authenticated()   // 각 경로 path별 권한 처리
                )
                .sessionManagement((session) -> session     // 세션 관리 기능 작동 - .maximunSessions(숫자)로 최대 허용 가능 세션 수를 정할 수 있음 (-1로 하면 무제한 허용)
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt 사용하는 경우 씀 (STATELESS는 인증 정보를 서버에 담지 않음))
                )
                //   .addFilterBefore(new JwtTokenFilter(userService, secretKey), UsernamePasswordAuthenticationFilter.class)
                //UserNamePasswordAuthenticationFilter 적용하기 전에 JWTTokenFilter를 적용하라는 뜻
                .build();
    }
}
