package org.example.shoppingweb.controller;

import org.example.shoppingweb.DTO.ChatMessage;
import org.example.shoppingweb.entity.Message;
import org.example.shoppingweb.repository.MessageRepository;
import org.example.shoppingweb.repository.UserRepository;
import org.example.shoppingweb.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat/send")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) return;

        Message message = new Message();
        message.setSenderId(chatMessage.getSenderId());
        message.setReceiverId(chatMessage.getReceiverId());
        message.setContent(chatMessage.getContent());
        message.setCreatedAt(Instant.now());
        message.setIsRead(false);
        messageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/messages/" + chatMessage.getReceiverId(), chatMessage);
    }

    @GetMapping("/api/chat/history/{userId}")
    @ResponseBody
    public List<Message> getChatHistory(@PathVariable Integer userId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer currentUserId;

        if (userDetails != null &&
                userDetails.getUser().getRole() != null &&
                "ROLE_ADMIN".equalsIgnoreCase(userDetails.getUser().getRole().getRoleName())) {
            currentUserId = 1;
        } else if (userDetails != null) {
            currentUserId = userDetails.getUser().getId();
        } else {
            currentUserId = 1;
        }

        return messageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                currentUserId, userId,
                userId, currentUserId
        );
    }


    @GetMapping("/api/chat/users")
    @ResponseBody
    public List<UserDTO> getUsersWhoMessagedAdmin() {
        List<Message> allMessages = messageRepository.findAll();

        // Chỉ lấy những người gửi tin nhắn cho admin (receiverId = 0)
        Set<Integer> senderIds = allMessages.stream()
                .filter(m -> m.getReceiverId() != null && m.getReceiverId() == 1)
                .map(Message::getSenderId)
                .collect(Collectors.toCollection(LinkedHashSet::new)); // giữ thứ tự

        return senderIds.stream()
                .map(id -> {
                    var user = userRepository.findById(id).orElse(null);
                    if (user != null) {
                        return new UserDTO(user.getId(), user.getUsername());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public record UserDTO(Integer id, String username) {
    }


}
