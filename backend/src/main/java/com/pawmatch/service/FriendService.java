package com.pawmatch.service;

import com.pawmatch.dto.response.FriendResponse;
import java.util.List;

public interface FriendService {
    void sendRequest(Long userId, Integer userType, Long friendId, Integer friendUserType);
    void acceptRequest(Long requestId, Long currentUserId);
    void rejectRequest(Long requestId, Long currentUserId);
    void deleteFriend(Long userId, Long friendId);
    List<FriendResponse> getFriends(Long userId, Integer userType);
    List<FriendResponse> getPendingRequests(Long userId, Integer userType);
    boolean areFriends(Long userId, Long friendId);
}
