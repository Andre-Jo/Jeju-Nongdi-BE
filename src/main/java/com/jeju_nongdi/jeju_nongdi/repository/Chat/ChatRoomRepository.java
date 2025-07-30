package com.jeju_nongdi.jeju_nongdi.repository.Chat;

import com.jeju_nongdi.jeju_nongdi.entity.Chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    List<ChatRoom> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);
    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.user1Id = :participant1Id AND cr.user2Id = :participant2Id) OR (cr.user1Id = :participant2Id AND cr.user2Id = :participant1Id)")
    List<ChatRoom> findRoomsByParticipantIds(@Param("participant1Id") Long participant1Id, @Param("participant2Id") Long participant2Id);
}