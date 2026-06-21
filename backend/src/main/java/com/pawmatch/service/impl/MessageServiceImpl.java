package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pawmatch.entity.Message;
import com.pawmatch.entity.Shelter;
import com.pawmatch.entity.User;
import com.pawmatch.exception.BusinessException;
import com.pawmatch.mapper.FriendMapper;
import com.pawmatch.mapper.MessageMapper;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.service.MessagePushService;
import com.pawmatch.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShelterMapper shelterMapper;

    @Autowired
    private MessagePushService messagePushService;

    @Override
    public Message send(Message message) {
        // 放宽限制：用户↔救助站免好友校验，用户↔用户仍需要好友
        boolean isUserToShelter = (message.getFromUserType() == 0 && message.getToUserType() == 1) ||
                                  (message.getFromUserType() == 1 && message.getToUserType() == 0);
        if (!isUserToShelter && friendMapper.findFriendship(message.getFromUserId(), message.getToUserId()) == null) {
            throw new BusinessException(403, "请先添加对方为好友");
        }
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // WebSocket 实时推送新消息给接收者
        try {
            messagePushService.pushNewMessage(message.getToUserId(), message);
        } catch (Exception ignored) {
            // 推送失败不影响消息存储
        }

        return message;
    }

    @Override
    public List<Message> getConversation(Long userId1, Integer userType1, Long userId2, Integer userType2) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
            .and(w1 -> w1.eq(Message::getFromUserId, userId1).eq(Message::getFromUserType, userType1)
                    .eq(Message::getToUserId, userId2).eq(Message::getToUserType, userType2))
            .or(w2 -> w2.eq(Message::getFromUserId, userId2).eq(Message::getFromUserType, userType2)
                    .eq(Message::getToUserId, userId1).eq(Message::getToUserType, userType1))
        );
        wrapper.orderByAsc(Message::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    @Override
    public List<Message> getConversationByAdoption(Long userId, Integer userType, Long adoptionId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getAdoptionId, adoptionId).and(w -> w
            .eq(Message::getFromUserId, userId).eq(Message::getFromUserType, userType)
            .or().eq(Message::getToUserId, userId).eq(Message::getToUserType, userType)
        );
        wrapper.orderByAsc(Message::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    @Override
    public List<Map<String, Object>> getConversationList(Long userId, Integer userType) {
        // 查询所有与该用户相关的消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
            .eq(Message::getFromUserId, userId).eq(Message::getFromUserType, userType)
            .or().eq(Message::getToUserId, userId).eq(Message::getToUserType, userType)
        );
        wrapper.orderByDesc(Message::getCreateTime);
        List<Message> allMsgs = messageMapper.selectList(wrapper);

        // 按对方去重，取最新一条
        Map<String, Map<String, Object>> convMap = new LinkedHashMap<>();
        for (Message msg : allMsgs) {
            boolean isFromMe = msg.getFromUserId().equals(userId) && msg.getFromUserType().equals(userType);
            Long otherUserId = isFromMe ? msg.getToUserId() : msg.getFromUserId();
            Integer otherUserType = isFromMe ? msg.getToUserType() : msg.getFromUserType();
            String key = otherUserId + "_" + otherUserType;
            if (!convMap.containsKey(key)) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("otherUserId", otherUserId);
                entry.put("otherUserType", otherUserType);
                entry.put("lastContent", msg.getContent());
                entry.put("lastTime", msg.getCreateTime().toString());
                entry.put("unread", !isFromMe && !Boolean.TRUE.equals(msg.getIsRead()));

                // 查询昵称
                String nickname = "";
                if (otherUserType == 1) {
                    Shelter shelter = shelterMapper.selectById(otherUserId);
                    if (shelter != null) nickname = shelter.getNickname();
                } else {
                    User u = userMapper.selectById(otherUserId);
                    if (u != null) nickname = u.getNickname();
                }
                entry.put("nickname", nickname);

                convMap.put(key, entry);
            }
        }
        return new ArrayList<>(convMap.values());
    }

    @Override
    public int getUnreadCount(Long userId, Integer userType) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToUserId, userId).eq(Message::getToUserType, userType)
               .eq(Message::getIsRead, false);
        return Math.toIntExact(messageMapper.selectCount(wrapper));
    }

    @Override
    public void markRead(Long userId, Integer userType, Long fromUserId) {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getToUserId, userId).eq(Message::getToUserType, userType)
               .eq(Message::getFromUserId, fromUserId)
               .eq(Message::getIsRead, false)
               .set(Message::getIsRead, true);
        messageMapper.update(null, wrapper);
    }
}
