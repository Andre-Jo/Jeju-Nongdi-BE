package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.*;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.ChatRoomDto;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.ChatRoomView;
import com.jeju_nongdi.jeju_nongdi.dto.Chat.MessageDto;
import com.jeju_nongdi.jeju_nongdi.entity.*;
import com.jeju_nongdi.jeju_nongdi.entity.Chat.ChatRoom;
import com.jeju_nongdi.jeju_nongdi.entity.Chat.Message;
import com.jeju_nongdi.jeju_nongdi.repository.*;
import com.jeju_nongdi.jeju_nongdi.repository.Chat.ChatRoomRepository;
import com.jeju_nongdi.jeju_nongdi.repository.Chat.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 1:1 채팅방 조회 또는 생성
     * 
     * @param currentUserEmail 현재 사용자 이메일
     * @param otherUserEmail 상대방 이메일
     * @return 채팅방 정보
     */
    public ChatRoomDto findOrCreate1to1ChatRoom(String currentUserEmail, String otherUserEmail) {
        log.debug("1:1 채팅방 조회/생성: 현재사용자={}, 상대방={}", currentUserEmail, otherUserEmail);
        
        // 입력값 검증
        if (!StringUtils.hasText(currentUserEmail) || !StringUtils.hasText(otherUserEmail)) {
            throw new IllegalArgumentException("사용자 이메일이 유효하지 않습니다");
        }
        
        if (currentUserEmail.equals(otherUserEmail)) {
            throw new IllegalArgumentException("자기 자신과는 채팅할 수 없습니다");
        }

        Long currentUserId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));
        Long otherUserId = userRepository.findIdByEmail(otherUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + otherUserEmail));

        String baseId = ChatRoom.generateBaseRoomId(currentUserId, otherUserId);

        ChatRoom room = chatRoomRepository.findById(baseId)
                .map(existing -> {
                    // 삭제된 채팅방을 다시 활성화
                    if (existing.getDeletedByUsers().remove(currentUserEmail)) {
                        chatRoomRepository.save(existing);
                        log.debug("삭제된 채팅방 재활성화: roomId={}, user={}", baseId, currentUserEmail);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    // 새 채팅방 생성
                    ChatRoom newRoom = ChatRoom.builder()
                            .roomId(baseId)
                            .user1Id(Math.min(currentUserId, otherUserId))
                            .user2Id(Math.max(currentUserId, otherUserId))
                            .build();
                    ChatRoom saved = chatRoomRepository.save(newRoom);
                    log.debug("새 채팅방 생성: roomId={}", baseId);
                    return saved;
                });

        Long otherId = room.getUser1Id().equals(currentUserId)
                ? room.getUser2Id()
                : room.getUser1Id();
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new IllegalArgumentException("상대 사용자를 찾을 수 없습니다: " + otherId));

        return ChatRoomDto.builder()
                .roomId(room.getRoomId())
                .user1Id(room.getUser1Id())
                .user2Id(room.getUser2Id())
                .createdAt(room.getCreatedAt())
                .otherUserNickname(other.getNickname())
                .otherUserProfileImage(other.getProfileImage())
                .build();
    }

    /**
     * 사용자의 모든 채팅방 조회
     * 
     * @param currentUserEmail 현재 사용자 이메일
     * @return 채팅방 목록 (마지막 메시지 포함)
     */
    @Transactional(readOnly = true)
    public List<ChatRoomView> getUserChatRooms(String currentUserEmail) {
        log.debug("사용자 채팅방 목록 조회: user={}", currentUserEmail);
        
        Long userId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));

        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId).stream()
                .filter(room -> !room.getDeletedByUsers().contains(currentUserEmail))
                .map(room -> {
                    Long otherId = room.getUser1Id().equals(userId)
                            ? room.getUser2Id()
                            : room.getUser1Id();
                    User other = userRepository.findById(otherId)
                            .orElseThrow(() -> new IllegalArgumentException("상대 사용자를 찾을 수 없습니다: " + otherId));
                    Optional<Message> lastMsg = messageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.getRoomId());
                    return new ChatRoomView(
                            room.getRoomId(),
                            room.getUser1Id(),
                            room.getUser2Id(),
                            room.getCreatedAt(),
                            lastMsg.map(Message::getContent).orElse(null),
                            lastMsg.map(Message::getCreatedAt).orElse(null),
                            other.getNickname(),
                            other.getProfileImage()
                    );
                })
                .sorted((a, b) -> {
                    // 마지막 메시지 시간으로 정렬 (최신순)
                    LocalDateTime timeA = a.getLastMessageTime() != null ? a.getLastMessageTime() : a.getCreatedAt();
                    LocalDateTime timeB = b.getLastMessageTime() != null ? b.getLastMessageTime() : b.getCreatedAt();
                    return timeB.compareTo(timeA);
                })
                .collect(Collectors.toList());
    }

    /**
     * 메시지 저장 및 실시간 브로드캐스트
     * 
     * @param roomId 채팅방 ID
     * @param senderEmail 발신자 이메일
     * @param content 메시지 내용
     */
    @Transactional
    public void saveAndBroadcastMessage(String roomId, String senderEmail, String content) {
        log.debug("메시지 저장 및 브로드캐스트: roomId={}, sender={}", roomId, senderEmail);
        
        // 입력값 검증
        if (!StringUtils.hasText(roomId) || !StringUtils.hasText(senderEmail) || !StringUtils.hasText(content)) {
            throw new IllegalArgumentException("필수 파라미터가 누락되었습니다");
        }
        
        // 메시지 길이 제한
        if (content.length() > 1000) {
            throw new IllegalArgumentException("메시지는 1000자를 초과할 수 없습니다");
        }

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + senderEmail));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 채팅방 참여자 권한 확인
        if (!Objects.equals(sender.getId(), room.getUser1Id()) && 
            !Objects.equals(sender.getId(), room.getUser2Id())) {
            throw new SecurityException("채팅방 참여 권한이 없습니다");
        }

        Long receiverId = room.getUser1Id().equals(sender.getId())
                ? room.getUser2Id()
                : room.getUser1Id();

        // XSS 방지를 위한 HTML 이스케이프 (이미 처리되었지만 추가 보안)
        String sanitizedContent = HtmlUtils.htmlEscape(content.trim());

        Message saved = messageRepository.save(Message.builder()
                .roomId(roomId)
                .senderId(sender.getId())
                .receiverId(receiverId)
                .content(sanitizedContent)
                .build());

        MessageDto dto = MessageDto.builder()
                .id(saved.getId())
                .roomId(saved.getRoomId())
                .senderId(saved.getSenderId())
                .receiverId(saved.getReceiverId())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .senderNickname(sender.getNickname())
                .senderProfileImage(sender.getProfileImage())
                .build();

        // 실시간 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, dto);

        // 수신자에게 알림 생성
        try {
            String receiverEmail = userRepository.findEmailById(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException("수신자 이메일을 찾을 수 없습니다: " + receiverId));

            notificationService.createChatNotification(
                    receiverEmail,
                    sender.getNickname() + " – " + (sanitizedContent.length() > 30 ? 
                        sanitizedContent.substring(0, 30) + "..." : sanitizedContent),
                    roomId,
                    sender.getNickname()
            );
        } catch (Exception e) {
            log.warn("알림 생성 실패: {}", e.getMessage());
            // 알림 실패가 메시지 전송을 방해하지 않도록 함
        }
        
        log.debug("메시지 저장 완료: messageId={}", saved.getId());
    }

    /**
     * 채팅방 메시지 조회 (페이징)
     * 
     * @param roomId 채팅방 ID
     * @param currentUserEmail 현재 사용자 이메일
     * @param pageable 페이징 정보
     * @return 페이징된 메시지 목록
     */
    @Transactional(readOnly = true)
    public Page<MessageDto> getMessagesByRoomWithPaging(String roomId, String currentUserEmail, Pageable pageable) {
        log.debug("채팅방 메시지 페이징 조회: roomId={}, user={}, page={}, size={}", 
            roomId, currentUserEmail, pageable.getPageNumber(), pageable.getPageSize());
        
        Long userId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 채팅방 참여 권한 확인
        if (!Objects.equals(userId, room.getUser1Id()) && !Objects.equals(userId, room.getUser2Id())) {
            throw new SecurityException("접근 거부: 참여자가 아닙니다");
        }
        
        // 삭제된 채팅방인지 확인
        if (room.getDeletedByUsers().contains(currentUserEmail)) {
            log.debug("삭제된 채팅방 메시지 조회: roomId={}, user={}", roomId, currentUserEmail);
            return Page.empty(pageable);
        }

        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable)
                .map(msg -> {
                    User sender = userRepository.findById(msg.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("보낸 사용자를 찾을 수 없습니다: " + msg.getSenderId()));
                    return MessageDto.builder()
                            .id(msg.getId())
                            .roomId(msg.getRoomId())
                            .senderId(msg.getSenderId())
                            .receiverId(msg.getReceiverId())
                            .content(msg.getContent())
                            .createdAt(msg.getCreatedAt())
                            .senderNickname(sender.getNickname())
                            .senderProfileImage(sender.getProfileImage())
                            .build();
                });
    }

    /**
     * 채팅방 메시지 조회 (전체 목록)
     * 
     * @param roomId 채팅방 ID
     * @param currentUserEmail 현재 사용자 이메일
     * @return 메시지 목록
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByRoom(String roomId, String currentUserEmail) {
        log.debug("채팅방 메시지 조회: roomId={}, user={}", roomId, currentUserEmail);
        
        Long userId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 채팅방 참여 권한 확인
        if (!Objects.equals(userId, room.getUser1Id()) && !Objects.equals(userId, room.getUser2Id())) {
            throw new SecurityException("접근 거부: 참여자가 아닙니다");
        }
        
        // 삭제된 채팅방인지 확인
        if (room.getDeletedByUsers().contains(currentUserEmail)) {
            log.debug("삭제된 채팅방 메시지 조회: roomId={}, user={}", roomId, currentUserEmail);
            return Collections.emptyList();
        }

        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(msg -> {
                    User sender = userRepository.findById(msg.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("보낸 사용자를 찾을 수 없습니다: " + msg.getSenderId()));
                    return MessageDto.builder()
                            .id(msg.getId())
                            .roomId(msg.getRoomId())
                            .senderId(msg.getSenderId())
                            .receiverId(msg.getReceiverId())
                            .content(msg.getContent())
                            .createdAt(msg.getCreatedAt())
                            .senderNickname(sender.getNickname())
                            .senderProfileImage(sender.getProfileImage())
                            .build();
                }).collect(Collectors.toList());
    }

    /**
     * 채팅방 소프트 삭제
     * 양쪽 사용자가 모두 삭제하면 완전 삭제됨
     * 
     * @param roomId 채팅방 ID
     * @param currentUserEmail 현재 사용자 이메일
     */
    @Transactional
    public void softDeleteRoom(String roomId, String currentUserEmail) {
        log.debug("채팅방 소프트 삭제: roomId={}, user={}", roomId, currentUserEmail);
        
        Long userId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 삭제 권한 확인
        if (!Objects.equals(userId, room.getUser1Id()) && !Objects.equals(userId, room.getUser2Id())) {
            throw new SecurityException("삭제 권한이 없습니다");
        }
        
        // 삭제 처리
        room.getDeletedByUsers().add(currentUserEmail);
        chatRoomRepository.save(room);

        // 양쪽 사용자가 모두 삭제했는지 확인
        String user1Email = userRepository.findEmailById(room.getUser1Id()).orElse(null);
        String user2Email = userRepository.findEmailById(room.getUser2Id()).orElse(null);
        
        if (user1Email != null && user2Email != null
                && room.getDeletedByUsers().contains(user1Email)
                && room.getDeletedByUsers().contains(user2Email)) {
            // 완전 삭제
            messageRepository.deleteAllByRoomId(roomId);
            chatRoomRepository.delete(room);
            log.debug("채팅방 완전 삭제: roomId={}", roomId);
        }
    }
}
