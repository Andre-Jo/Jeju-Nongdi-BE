package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.NotificationDto;
import com.jeju_nongdi.jeju_nongdi.entity.Notification;
import com.jeju_nongdi.jeju_nongdi.entity.Notification.NotificationType;
import com.jeju_nongdi.jeju_nongdi.repository.NotificationRepository;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final SimpMessageSendingOperations template;
    private final UserRepository userRepository;

    private Long resolveUserId(String email) {
        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    /** 채팅 알림 생성 (roomId, senderNickname 전달) */
    public NotificationDto createChatNotification(String userEmail, String message, String roomId, String senderNickname) {
        Long userId = resolveUserId(userEmail);
        Notification notification = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.CHAT)
                .message(message)
                .read(false)
                .roomId(roomId)
                .senderNickname(senderNickname)
                .build();
        notification = repo.save(notification);

        NotificationDto dto = NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .roomId(roomId)
                .senderNickname(senderNickname)
                .postId(null)
                .commentId(null)
                .build();
        template.convertAndSend("/topic/notifications/" + userId, dto);
        return dto;
    }

    /** 댓글 알림 생성 (postId, commentId 전달) */
    public NotificationDto createCommentNotification(String userEmail, String message, Long postId, Long commentId) {
        Long userId = resolveUserId(userEmail);
        Notification notification = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.COMMENT)
                .message(message)
                .read(false)
                .postId(postId)
                .commentId(commentId)
                .build();
        notification = repo.save(notification);

        NotificationDto dto = NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .roomId(null)
                .postId(postId)
                .commentId(commentId)
                .build();
        template.convertAndSend("/topic/notifications/" + userId, dto);
        return dto;
    }

    /** 댓글 채택 알림 생성 (postId, commentId 전달) */
    public NotificationDto AdoptCommentNotification(String userEmail, String message, Long postId, Long commentId) {
        Long userId = resolveUserId(userEmail);
        Notification notification = Notification.builder()
                .userId(userId)
                .type(NotificationType.COMMENT_ADOPTED)
                .message(message)
                .read(false)
                .postId(postId)
                .commentId(commentId)
                .build();
        notification = repo.save(notification);

        NotificationDto dto = NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .roomId(null)
                .postId(postId)
                .commentId(commentId)
                .build();
        template.convertAndSend("/topic/notifications/" + userId, dto);
        return dto;
    }

    /** 좋아요 알림 생성 (postId, commentId 옵션) */
    public NotificationDto createLikeNotification(String userEmail, String message, Long postId, Long commentId) {
        Long userId = resolveUserId(userEmail);
        Notification notification = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.LIKE)
                .message(message)
                .read(false)
                .postId(postId)
                .commentId(commentId)
                .build();
        notification = repo.save(notification);

        NotificationDto dto = NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .roomId(null)
                .postId(postId)
                .commentId(commentId)
                .build();
        template.convertAndSend("/topic/notifications/" + userId, dto);
        return dto;
    }

    /** 유저의 모든 알림 조회 */
    public List<NotificationDto> findAll(String userEmail) {
        Long userId = resolveUserId(userEmail);
        return repo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> NotificationDto.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .message(n.getMessage())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .senderNickname(n.getSenderNickname())
                        .roomId(n.getRoomId())
                        .postId(n.getPostId())
                        .commentId(n.getCommentId())
                        .build()
                )
                .collect(Collectors.toList());
    }

    /** 알림 읽음 처리 */
    @Transactional
    public void markAsRead(Long notificationId, String userEmail) {
        Long userId = resolveUserId(userEmail);
        Notification notification = repo.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new AccessDeniedException("본인의 알림이 아닙니다."));
        notification.setRead(true);
    }
}