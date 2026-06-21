package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendResponse {
    private Long id;          // friend relationship id
    private Long friendId;    // the other user's id
    private Integer friendUserType;
    private String nickname;
    private String avatar;
    private Integer status;   // 0=pending, 1=accepted, 2=rejected
    private LocalDateTime createTime;
}
