package com.jeju_nongdi.jeju_nongdi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * 
 * STOMP 프로토콜을 사용한 실시간 채팅 기능을 위한 WebSocket 설정
 * 
 * 주요 기능:
 * - WebSocket 연결 엔드포인트 설정 (/api/ws)
 * - 메시지 브로커 설정 (/topic 구독, /app 전송)
 * - CORS 설정
 * - SockJS 폴백 지원
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String frontendBaseUrl;

    /**
     * TaskScheduler Bean 등록
     * WebSocket heartbeat 처리를 위해 필요합니다.
     */
    @Bean
    public TaskScheduler messagingTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * WebSocket 연결 엔드포인트 등록
     * 
     * 클라이언트가 WebSocket 연결을 위해 접속하는 엔드포인트를 설정합니다.
     * SockJS를 활성화하여 WebSocket을 지원하지 않는 브라우저에서도 동작하도록 합니다.
     * 
     * 연결 URL: /api/ws
     * SockJS 연결 URL: /api/ws/sockjs
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws")
                .setAllowedOrigins(frontendBaseUrl)
                .withSockJS()
                .setHeartbeatTime(25000) // 25초마다 하트비트
                .setSessionCookieNeeded(false) // 쿠키 불필요
                .setDisconnectDelay(30000); // 30초 후 연결 해제

        log.info("WebSocket 엔드포인트 등록 완료: /api/ws, 허용 Origin: {}", frontendBaseUrl);
    }

    /**
     * 메시지 브로커 설정
     * 
     * - /topic: 클라이언트가 구독할 수 있는 주제 (브로드캐스트용)
     * - /app: 클라이언트가 서버로 메시지를 보낼 때 사용하는 접두사
     * 
     * 채팅 예시:
     * - 구독: /topic/chat/room/{roomId}
     * - 전송: /app/chat.sendPrivateMessage
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정 - 클라이언트가 구독할 주제
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{25000, 25000}) // 서버/클라이언트 하트비트 25초
                .setTaskScheduler(messagingTaskScheduler()); // TaskScheduler 설정

        // 애플리케이션 목적지 접두사 - 클라이언트가 서버로 메시지 전송시 사용
        registry.setApplicationDestinationPrefixes("/app");
        
        // 사용자별 개인 메시지 접두사 (필요시 사용)
        registry.setUserDestinationPrefix("/user");

        log.info("메시지 브로커 설정 완료: 구독 접두사=/topic, 전송 접두사=/app");
    }
}
