package com.pawmatch.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShelterResponse {
    private Long id;
    private String nickname;
    private String managerName;
    private String phone;
    private String province;
    private String city;
    private String addressDetail;
    private String introduction;
    private Integer creditScore;
    private Boolean infoComplete;
    private LocalDateTime createTime;
}