package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.*;
import com.jeju_nongdi.jeju_nongdi.entity.*;
import com.jeju_nongdi.jeju_nongdi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final MentoringRepository mentoringRepository;
    private final IdleFarmlandRepository idleFarmlandRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅방 생성 또는 기존 채팅방 반환
     */
    public ChatRoomResponse createOrGetChatRoom(ChatRoomCreateRequest request, UserDetails userDetails) {
        log.info("Creating or getting chat room for type: {}, referenceId: {}", 
                request.getChatType(), request.getReferenceId());

        User creator = getUserByEmail(userDetails.getUsername());
        User participant = userRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new EntityNotFoundException("참여자를 찾을 수 없습니다."));

        // 기존 채팅방 확인
        return chatRoomRepository.findExistingChatRoom(
                request.getChatType(), request.getReferenceId(), creator, participant)
                .map(existingRoom -> {
                    log.info("Found existing chat room: {}", existingRoom.getRoomId());
                    return ChatRoomResponse.from(existingRoom, creator);
                })
                .orElseGet(() -> {
                    // 새 채팅방 생성
                    ChatRoom newRoom = createNewChatRoom(request, creator, participant);
                    
                    // 첫 메시지가 있으면 전송
                    if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
                        sendMessage(newRoom.getRoomId(), 
                                new ChatMessageRequest(request.getInitialMessage(), ChatMessage.MessageType.CHAT), 
                                userDetails);
                    }
                    
                    return ChatRoomResponse.from(newRoom, creator);
                });
    }

    /**
     * 새 채팅방 생성
     */
    private ChatRoom createNewChatRoom(ChatRoomCreateRequest request, User creator, User participant) {
        String roomId = UUID.randomUUID().toString();
        String title = generateChatRoomTitle(request.getChatType(), request.getReferenceId());

        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .title(title)
                .chatType(request.getChatType())
                .referenceId(request.getReferenceId())
                .creator(creator)
                .participant(participant)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        log.info("Created new chat room: {}", savedRoom.getRoomId());

        return savedRoom;
    }

    /**
     * 채팅방 제목 생성
     */
    private String generateChatRoomTitle(ChatRoom.ChatType chatType, Long referenceId) {
        switch (chatType) {
            case MENTORING:
                return mentoringRepository.findById(referenceId)
                        .map(m -> "멘토링: " + m.getTitle())
                        .orElse("멘토링 상담");
            case FARMLAND:
                return idleFarmlandRepository.findById(referenceId)
                        .map(f -> "농지문의: " + f.getTitle())
                        .orElse("농지 문의");
            case JOB_POSTING:
                return "일자리 문의";
            default:
                return "채팅방";
        }
    }

    /**
     * 메시지 전송
     */
    public ChatMessageResponse sendMessage(String roomId, ChatMessageRequest request, UserDetails userDetails) {
        log.info("Sending message to room: {}", roomId);

        User sender = getUserByEmail(userDetails.getUsername());
        ChatRoom chatRoom = getChatRoomByRoomId(roomId);

        // 채팅방 참여자 확인
        if (!chatRoom.isParticipant(sender)) {
            throw new AccessDeniedException("채팅방에 참여할 권한이 없습니다.");
        }

        // 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 채팅방 마지막 메시지 업데이트
        chatRoom.updateLastMessage(request.getContent());
        chatRoomRepository.save(chatRoom);

        // WebSocket으로 실시간 전송
        WebSocketChatMessage wsMessage = new WebSocketChatMessage(
                roomId,
                sender.getName(),
                request.getContent(),
                request.getMessageType(),
                savedMessage.getSentAt()
        );

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, wsMessage);

        // 상대방에게 개별 알림 전송
        User otherParticipant = chatRoom.getOtherParticipant(sender);
        messagingTemplate.convertAndSendToUser(
                otherParticipant.getEmail(),
                "/queue/notifications",
                wsMessage
        );

        return ChatMessageResponse.from(savedMessage, sender);
    }

    /**
     * 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomListResponse getChatRooms(UserDetails userDetails) {
        log.info("Fetching chat rooms for user: {}", userDetails.getUsername());

        User user = getUserByEmail(userDetails.getUsername());
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserOrderByLastMessageTimeDesc(user);

        List<ChatRoomResponse> chatRoomResponses = chatRooms.stream()
                .map(room -> {
                    ChatRoomResponse response = ChatRoomResponse.from(room, user);
                    // 읽지 않은 메시지 수 설정
                    Long unreadCount = chatMessageRepository.countUnreadMessages(room, user);
                    response.setUnreadCount(unreadCount);
                    return response;
                })
                .collect(Collectors.toList());

        // 전체 읽지 않은 메시지 수
        Long totalUnreadCount = chatMessageRepository.countUnreadMessagesByUser(user);

        return ChatRoomListResponse.of(chatRoomResponses, totalUnreadCount);
    }

    /**
     * 채팅방 메시지 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getChatMessages(String roomId, Pageable pageable, UserDetails userDetails) {
        log.info("Fetching messages for room: {}", roomId);

        User user = getUserByEmail(userDetails.getUsername());
        ChatRoom chatRoom = getChatRoomByRoomId(roomId);

        // 채팅방 참여자 확인
        if (!chatRoom.isParticipant(user)) {
            throw new AccessDeniedException("채팅방에 접근할 권한이 없습니다.");
        }

        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderBySentAtDesc(chatRoom, pageable);
        return messages.map(message -> ChatMessageResponse.from(message, user));
    }

    /**
     * 채팅방 메시지 읽음 처리
     */
    public void markMessagesAsRead(String roomId, UserDetails userDetails) {
        log.info("Marking messages as read for room: {}", roomId);

        User user = getUserByEmail(userDetails.getUsername());
        ChatRoom chatRoom = getChatRoomByRoomId(roomId);

        // 채팅방 참여자 확인
        if (!chatRoom.isParticipant(user)) {
            throw new AccessDeniedException("채팅방에 접근할 권한이 없습니다.");
        }

        chatMessageRepository.markAllAsReadInChatRoom(chatRoom, user);
    }

    /**
     * 채팅방 나가기
     */
    public void leaveChatRoom(String roomId, UserDetails userDetails) {
        log.info("User leaving chat room: {}", roomId);

        User user = getUserByEmail(userDetails.getUsername());
        ChatRoom chatRoom = getChatRoomByRoomId(roomId);

        // 채팅방 참여자 확인
        if (!chatRoom.isParticipant(user)) {
            throw new AccessDeniedException("채팅방에 접근할 권한이 없습니다.");
        }

        // 나가기 메시지 전송
        WebSocketChatMessage leaveMessage = WebSocketChatMessage.createLeaveMessage(roomId, user.getName());
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, leaveMessage);

        // 시스템 메시지 저장
        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .content(user.getName() + "님이 채팅방을 나갔습니다.")
                .messageType(ChatMessage.MessageType.LEAVE)
                .build();

        chatMessageRepository.save(systemMessage);
    }

    /**
     * 채팅방 입장
     */
    public void enterChatRoom(String roomId, UserDetails userDetails) {
        log.info("User entering chat room: {}", roomId);

        User user = getUserByEmail(userDetails.getUsername());
        ChatRoom chatRoom = getChatRoomByRoomId(roomId);

        // 채팅방 참여자 확인
        if (!chatRoom.isParticipant(user)) {
            throw new AccessDeniedException("채팅방에 접근할 권한이 없습니다.");
        }

        // 입장 메시지 전송
        WebSocketChatMessage enterMessage = WebSocketChatMessage.createEnterMessage(roomId, user.getName());
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, enterMessage);

        // 읽지 않은 메시지 읽음 처리
        markMessagesAsRead(roomId, userDetails);
    }

    /**
     * 읽지 않은 메시지 총 개수 조회
     */
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(UserDetails userDetails) {
        User user = getUserByEmail(userDetails.getUsername());
        return chatMessageRepository.countUnreadMessagesByUser(user);
    }

    /**
     * 파일과 함께 메시지 전송
     */
    public ChatMessageResponse sendMessageWithFile(String roomId, ChatMessageRequest request, 
                                                  MultipartFile file, UserDetails userDetails) {
        log.info("Sending message with file to room: {}", roomId);

        User sender = getUserByEmail(userDetails.getUsername());
        ChatRoom chatRoom = getChatRoomByRoomId(roomId);

        // 채팅방 참여자 확인
        if (!chatRoom.isParticipant(sender)) {
            throw new AccessDeniedException("채팅방에 참여할 권한이 없습니다.");
        }

        String content = request.getContent();
        ChatMessage.MessageType messageType = request.getMessageType();

        // 파일이 있는 경우 파일 정보를 메시지에 추가
        if (file != null && !file.isEmpty()) {
            try {
                // 파일 저장 로직 (실제 구현 시 파일 저장 서비스 사용)
                String fileName = file.getOriginalFilename();
                String fileSize = formatFileSize(file.getSize());
                
                // 파일 정보를 메시지에 포함
                content = String.format("[파일] %s (%s)%s%s", 
                    fileName, fileSize, 
                    content != null && !content.trim().isEmpty() ? "\n" + content : "",
                    content != null && !content.trim().isEmpty() ? "" : ""
                );
                
                messageType = ChatMessage.MessageType.CHAT; // 파일 메시지도 일반 채팅으로 처리
                
            } catch (Exception e) {
                log.error("File processing error: {}", e.getMessage());
                throw new RuntimeException("파일 처리 중 오류가 발생했습니다.");
            }
        }

        // 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .messageType(messageType)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 채팅방 마지막 메시지 업데이트
        chatRoom.updateLastMessage(content);
        chatRoomRepository.save(chatRoom);

        // WebSocket으로 실시간 전송
        WebSocketChatMessage wsMessage = new WebSocketChatMessage(
                roomId,
                sender.getName(),
                content,
                messageType,
                savedMessage.getSentAt()
        );

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, wsMessage);

        // 상대방에게 개별 알림 전송
        User otherParticipant = chatRoom.getOtherParticipant(sender);
        messagingTemplate.convertAndSendToUser(
                otherParticipant.getEmail(),
                "/queue/notifications",
                wsMessage
        );

        return ChatMessageResponse.from(savedMessage, sender);
    }

    /**
     * 특정 타입의 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomsByType(ChatRoom.ChatType chatType, UserDetails userDetails) {
        User user = getUserByEmail(userDetails.getUsername());
        
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserOrderByLastMessageTimeDesc(user)
                .stream()
                .filter(room -> room.getChatType() == chatType)
                .collect(Collectors.toList());

        return chatRooms.stream()
                .map(room -> ChatRoomResponse.from(room, user))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 검색
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> searchChatRooms(String keyword, UserDetails userDetails) {
        User user = getUserByEmail(userDetails.getUsername());
        
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserOrderByLastMessageTimeDesc(user)
                .stream()
                .filter(room -> room.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                               (room.getLastMessage() != null && 
                                room.getLastMessage().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());

        return chatRooms.stream()
                .map(room -> ChatRoomResponse.from(room, user))
                .collect(Collectors.toList());
    }

    /**
     * 파일 크기 포맷팅
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // Private helper methods
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    private ChatRoom getChatRoomByRoomId(String roomId) {
        return chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다: " + roomId));
    }
}
