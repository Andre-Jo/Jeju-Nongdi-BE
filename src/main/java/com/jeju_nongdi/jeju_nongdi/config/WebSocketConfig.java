package com.jeju_nongdi.jeju_nongdi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketChannelInterceptor webSocketChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        config.enableSimpleBroker("/topic", "/queue") // 구독 경로
                .setHeartbeatValue(new long[]{10000, 20000}) // 하트비트 설정 (클라이언트, 서버)
                .setTaskScheduler(null); // 기본 스케줄러 사용
        
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 메시지 발송 시 경로
        config.setUserDestinationPrefix("/user"); // 개별 사용자에게 메시지 전송 시 경로
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // CORS 설정 (개발 환경)
                .withSockJS() // SockJS 지원 (WebSocket 미지원 브라우저 대응)
                .setSessionCookieNeeded(false) // 세션 쿠키 불필요
                .setHeartbeatTime(25000); // SockJS 하트비트 (25초)
                
        // WebSocket 네이티브 엔드포인트 (SockJS 없이)
        registry.addEndpoint("/ws-chat-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트로부터 받는 메시지 채널에 인터셉터 등록
        registration.interceptors(webSocketChannelInterceptor);
    }
}
