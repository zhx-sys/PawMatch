package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String account;
    private String nickname;
    private String realName;
    private String phone;
    private String province;
    private String city;
    private String addressDetail;
    private String petPreference;
    private String livingSpace;
    private Boolean hasChildren;
    private Boolean hasOtherPets;
    private String petExperience;
    private String dailyRoutine;
    private String budgetRange;
    private String birthday;
    private Boolean matchingProfileComplete;
    private Integer userType;
    private Integer creditScore;
    private Boolean infoComplete;
    private LocalDateTime createTime;
}