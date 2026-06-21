package com.pawmatch.controller;

import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.entity.Message;
import com.pawmatch.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ApiResponse<Message> send(@RequestBody Message message) {
        return ApiResponse.success(messageService.send(message));
    }

    @GetMapping("/conversation/{otherUserId}/{otherUserType}")
    public ApiResponse<java.util.List<Message>> getConversation(
            @RequestParam Long userId,
            @RequestParam Integer userType,
            @PathVariable Long otherUserId,
            @PathVariable Integer otherUserType) {
        return ApiResponse.success(messageService.getConversation(userId, userType, otherUserId, otherUserType));
    }

    @GetMapping("/adoption/{adoptionId}")
    public ApiResponse<java.util.List<Message>> getByAdoption(
            @RequestParam Long userId,
            @RequestParam Integer userType,
            @PathVariable Long adoptionId) {
        return ApiResponse.success(messageService.getConversationByAdoption(userId, userType, adoptionId));
    }

    @GetMapping("/conversations")
    public ApiResponse<java.util.List<java.util.Map<String, Object>>> getConversationList(
            @RequestParam Long userId,
            @RequestParam Integer userType) {
        return ApiResponse.success(messageService.getConversationList(userId, userType));
    }

    @GetMapping("/unread")
    public ApiResponse<Map<String, Integer>> getUnreadCount(
            @RequestParam Long userId,
            @RequestParam Integer userType) {
        int count = messageService.getUnreadCount(userId, userType);
        return ApiResponse.success(Map.of("count", count));
    }

    @PutMapping("/read/{fromUserId}")
    public ApiResponse<Void> markRead(
            @RequestParam Long userId,
            @RequestParam Integer userType,
            @PathVariable Long fromUserId) {
        messageService.markRead(userId, userType, fromUserId);
        return ApiResponse.success(null);
    }
}
