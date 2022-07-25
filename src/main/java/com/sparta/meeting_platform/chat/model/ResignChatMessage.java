package com.sparta.meeting_platform.chat.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ResignChatMessage {
    // 메시지 타입 : 입장, 채팅, 나가기
    public enum MessageType {
        ENTER, TALK, QUIT
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String roomId; // 방번호 (postId)
    @Enumerated(EnumType.STRING)
    @Column
    private ChatMessage.MessageType type; // 메시지 타입
    @Column
    private String sender; // nickname
    @Column
    private String message; // 메시지
    @Column
    private String profileUrl;
    @Column
    private Long enterUserCnt;
    @Column
    private Long userId;
    @Column
    private LocalDateTime createdAt;
    @Column
    private String fileUrl;
    @JoinColumn(name = "CHAT_ROOM_ID")
    @ManyToOne
    private ResignChatRoom chatRoom;

    public ResignChatMessage(ChatMessage chatMessage, ResignChatRoom chatRoom) {
        this.roomId = chatMessage.getRoomId();
        this.type = chatMessage.getType();
        this.sender = chatMessage.getSender();
        this.message = chatMessage.getMessage();
        this.profileUrl = chatMessage.getProfileUrl();
        this.enterUserCnt = chatMessage.getEnterUserCnt();
        this.userId = chatMessage.getUserId();
        this.createdAt = chatMessage.getCreatedAt();
        this.fileUrl = chatMessage.getFileUrl();
        this.chatRoom = chatRoom;
    }
}
