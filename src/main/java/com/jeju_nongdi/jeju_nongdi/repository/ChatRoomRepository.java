package com.jeju_nongdi.jeju_nongdi.repository;

import com.jeju_nongdi.jeju_nongdi.entity.ChatRoom;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // 사용자가 참여한 채팅방 목록 (최신순)
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "(cr.creator = :user OR cr.participant = :user) AND cr.isActive = true " +
           "ORDER BY cr.lastMessageTime DESC, cr.createdAt DESC")
    List<ChatRoom> findByUserOrderByLastMessageTimeDesc(@Param("user") User user);
    
    // 특정 타입의 채팅방에서 두 사용자 간 기존 채팅방 확인
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "cr.chatType = :chatType AND cr.referenceId = :referenceId AND " +
           "((cr.creator = :user1 AND cr.participant = :user2) OR " +
           "(cr.creator = :user2 AND cr.participant = :user1)) AND " +
           "cr.isActive = true")
    Optional<ChatRoom> findExistingChatRoom(
            @Param("chatType") ChatRoom.ChatType chatType,
            @Param("referenceId") Long referenceId,
            @Param("user1") User user1,
            @Param("user2") User user2
    );
    
    // 룸 ID로 채팅방 찾기
    Optional<ChatRoom> findByRoomId(String roomId);
    
    // 특정 타입의 채팅방 목록
    List<ChatRoom> findByChatTypeAndIsActiveOrderByCreatedAtDesc(
            ChatRoom.ChatType chatType, Boolean isActive);
}
