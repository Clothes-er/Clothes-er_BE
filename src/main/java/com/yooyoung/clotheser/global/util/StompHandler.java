package com.yooyoung.clotheser.global.util;

import com.yooyoung.clotheser.global.entity.BaseException;
import com.yooyoung.clotheser.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    // WebSocket을 통해 들어온 요청이 처리되기 전에 실행됨
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        // WebSocket 연결 요청
        if (StompCommand.CONNECT == accessor.getCommand()) {
            String jwt = accessor.getFirstNativeHeader("Authorization");

            try {
                if (StringUtils.hasText(jwt) && jwt.startsWith("Bearer ")) {
                    jwt = jwt.substring(7);
                }

                if (jwtProvider.validateToken(jwt)) {
                    Authentication authentication = jwtProvider.getAuthentication(jwt);
                    accessor.setUser(authentication);
                }
            } catch (BaseException e) {
                throw new RuntimeException(e);
            }
        }

        return message;
    }
}
