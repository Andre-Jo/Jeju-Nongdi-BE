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
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public ChatRoomDto findOrCreate1to1ChatRoom(String currentUserEmail, String otherUserEmail) {
        Long currentUserId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));
        Long otherUserId = userRepository.findIdByEmail(otherUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + otherUserEmail));

        String baseId = ChatRoom.generateBaseRoomId(currentUserId, otherUserId);

        ChatRoom room = chatRoomRepository.findById(baseId)
                .map(existing -> {
                    if (existing.getDeletedByUsers().remove(currentUserEmail)) {
                        chatRoomRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    ChatRoom nr = ChatRoom.builder()
                            .roomId(baseId)
                            .user1Id(Math.min(currentUserId, otherUserId))
                            .user2Id(Math.max(currentUserId, otherUserId))
                            .build();
                    return chatRoomRepository.save(nr);
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

    public List<ChatRoomView> getUserChatRooms(String currentUserEmail) {
        Long userId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));

        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId).stream()
                .filter(r -> !r.getDeletedByUsers().contains(currentUserEmail))
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
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveAndBroadcastMessage(String roomId, String senderEmail, String content) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + senderEmail));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        Long receiverId = room.getUser1Id().equals(sender.getId())
                ? room.getUser2Id()
                : room.getUser1Id();

        Message saved = messageRepository.save(Message.builder()
                .roomId(roomId)
                .senderId(sender.getId())
                .receiverId(receiverId)
                .content(content)
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

        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, dto);

        String receiverEmail = userRepository.findEmailById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("수신자 이메일을 찾을 수 없습니다: " + receiverId));

        notificationService.createChatNotification(
                receiverEmail,
                sender.getNickname() + " – " + content,
                roomId,
                sender.getNickname()
        );
    }

    public List<MessageDto> getMessagesByRoom(String roomId, String currentUserEmail) {
        Long userId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        if (!Objects.equals(userId, room.getUser1Id()) && !Objects.equals(userId, room.getUser2Id())) {
            throw new SecurityException("접근 거부: 참여자가 아닙니다.");
        }
        if (room.getDeletedByUsers().contains(currentUserEmail)) {
            return Collections.emptyList();
        }

        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(msg -> {
                    User snd = userRepository.findById(msg.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("보낸 사용자를 찾을 수 없습니다: " + msg.getSenderId()));
                    return MessageDto.builder()
                            .id(msg.getId())
                            .roomId(msg.getRoomId())
                            .senderId(msg.getSenderId())
                            .receiverId(msg.getReceiverId())
                            .content(msg.getContent())
                            .createdAt(msg.getCreatedAt())
                            .senderNickname(snd.getNickname())
                            .senderProfileImage(snd.getProfileImage())
                            .build();
                }).collect(Collectors.toList());
    }

    @Transactional
    public void softDeleteRoom(String roomId, String currentUserEmail) {
        Long userId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        if (!Objects.equals(userId, room.getUser1Id()) && !Objects.equals(userId, room.getUser2Id())) {
            throw new SecurityException("삭제 권한 없음.");
        }
        room.getDeletedByUsers().add(currentUserEmail);
        chatRoomRepository.save(room);

        String u1 = userRepository.findEmailById(room.getUser1Id()).orElse(null);
        String u2 = userRepository.findEmailById(room.getUser2Id()).orElse(null);
        if (u1 != null && u2 != null
                && room.getDeletedByUsers().contains(u1)
                && room.getDeletedByUsers().contains(u2)) {
            messageRepository.deleteAllByRoomId(roomId);
            chatRoomRepository.delete(room);
        }
    }
}