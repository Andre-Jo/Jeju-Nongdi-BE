package com.jeju_nongdi.jeju_nongdi.repository;

import com.jeju_nongdi.jeju_nongdi.entity.ChatMessage;
import com.jeju_nongdi.jeju_nongdi.entity.ChatRoom;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 특정 채팅방의 메시지 목록 (페이징)
    Page<ChatMessage> findByChatRoomOrderBySentAtDesc(ChatRoom chatRoom, Pageable pageable);
    
    // 특정 채팅방의 최근 메시지들
    List<ChatMessage> findTop50ByChatRoomOrderBySentAtDesc(ChatRoom chatRoom);
    
    // 읽지 않은 메시지 개수
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE " +
           "cm.chatRoom = :chatRoom AND cm.sender != :user AND cm.isRead = false")
    Long countUnreadMessages(@Param("chatRoom") ChatRoom chatRoom, @Param("user") User user);
    
    // 사용자의 전체 읽지 않은 메시지 개수
    @Query("SELECT COUNT(cm) FROM ChatMessage cm JOIN cm.chatRoom cr WHERE " +
           "(cr.creator = :user OR cr.participant = :user) AND " +
           "cm.sender != :user AND cm.isRead = false")
    Long countUnreadMessagesByUser(@Param("user") User user);
    
    // 특정 채팅방의 메시지를 모두 읽음 처리
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE " +
           "cm.chatRoom = :chatRoom AND cm.sender != :user AND cm.isRead = false")
    void markAllAsReadInChatRoom(@Param("chatRoom") ChatRoom chatRoom, @Param("user") User user);
    
    // 마지막 메시지 조회
    Optional<ChatMessage> findTopByChatRoomOrderBySentAtDesc(ChatRoom chatRoom);
}
