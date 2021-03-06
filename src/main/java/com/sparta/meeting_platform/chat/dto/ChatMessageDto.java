package com.sparta.meeting_platform.chat.dto;


import com.sparta.meeting_platform.chat.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private ChatMessage.MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String message; // 메시지
    private String sender; // nickname
    private String profileUrl;
    private Long enterUserCnt;
    private Long userId;
    private String createdAt;
    private String fileUrl;
    private Boolean quitOwner;


    public ChatMessageDto(ChatMessage chatMessage, String createdAt) {
        this.type = chatMessage.getType();
        this.roomId = chatMessage.getRoomId();
        this.message = chatMessage.getMessage();
        this.sender = chatMessage.getSender();
        this.profileUrl = chatMessage.getProfileUrl();
        this.enterUserCnt = chatMessage.getEnterUserCnt();
        this.userId = chatMessage.getUserId();
        this.createdAt = createdAt;
        this.fileUrl = chatMessage.getFileUrl();
        this.quitOwner = chatMessage.getQuitOwner();
    }
}