package org.example.shoppingweb.DTO;

import lombok.Data;

@Data
public class ChatMessage {
    private Integer senderId;
    private Integer receiverId;
    private String content;
}
