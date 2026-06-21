package com.pawmatch.service;

import com.pawmatch.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {
    Message send(Message message);
    List<Message> getConversation(Long userId1, Integer userType1, Long userId2, Integer userType2);
    List<Message> getConversationByAdoption(Long userId, Integer userType, Long adoptionId);
    List<Map<String, Object>> getConversationList(Long userId, Integer userType);
    int getUnreadCount(Long userId, Integer userType);
    void markRead(Long userId, Integer userType, Long fromUserId);
}
