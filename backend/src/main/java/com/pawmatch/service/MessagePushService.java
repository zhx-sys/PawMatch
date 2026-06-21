package com.pawmatch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息推送服务：通过 WebSocket (STOMP) 向指定用户的私有频道推送实时消息。
 * 推送目的地：/user/{userId}/queue/messages
 */
@Service
public class MessagePushService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public MessagePushService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 推送原始对象到用户私有频道（自动序列化）
     */
    private void push(Long userId, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId), "/queue/messages", json);
        } catch (JsonProcessingException e) {
            // 推送失败不影响主流程
        }
    }

    // ==================== 各类事件推送 ====================

    /** 新聊天消息（发给接收者） */
    public void pushNewMessage(Long receiverUserId, Object messageData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_MESSAGE");
        payload.put("data", messageData);
        push(receiverUserId, payload);
    }

    /** 新好友请求（发给被请求方） */
    public void pushNewFriendRequest(Long receiverUserId, Object requestData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_FRIEND_REQUEST");
        payload.put("data", requestData);
        push(receiverUserId, payload);
    }

    /** 好友请求被接受（发给请求发起方） */
    public void pushFriendRequestAccepted(Long receiverUserId, Object friendData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FRIEND_REQUEST_ACCEPTED");
        payload.put("data", friendData);
        push(receiverUserId, payload);
    }

    /** 新领养申请（发给救助站） */
    public void pushNewAdoption(Long shelterUserId, Object adoptionData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_ADOPTION");
        payload.put("data", adoptionData);
        push(shelterUserId, payload);
    }

    /** 领养申请状态变更（发给申请人） */
    public void pushAdoptionStatusChange(Long applicantUserId, Object adoptionData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ADOPTION_STATUS");
        payload.put("data", adoptionData);
        push(applicantUserId, payload);
    }

    /** 寄养预约创建（发给救助站） */
    public void pushFosterOrderCreated(Long shelterUserId, Object orderData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FOSTER_ORDER_CREATED");
        payload.put("data", orderData);
        push(shelterUserId, payload);
    }

    /** 寄养预约状态变更（发给用户/救助站） */
    public void pushFosterOrderUpdated(Long receiverUserId, Object orderData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FOSTER_ORDER_UPDATED");
        payload.put("data", orderData);
        push(receiverUserId, payload);
    }

    /** 新评论（发给帖子作者） */
    public void pushNewComment(Long postAuthorUserId, Object commentData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_COMMENT");
        payload.put("data", commentData);
        push(postAuthorUserId, payload);
    }

    /** 新帖子（发给粉丝/关注者） */
    public void pushNewPost(Long followerUserId, Object postData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_POST");
        payload.put("data", postData);
        push(followerUserId, payload);
    }

    /** 通用通知推送 */
    public void pushNotification(Long receiverUserId, String notificationType, Object data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", notificationType);
        payload.put("data", data);
        push(receiverUserId, payload);
    }
}
