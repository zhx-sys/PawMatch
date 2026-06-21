package com.pawmatch.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String nickname;
    private String realName;
    private String idCard;
    private String phone;
    private String province;
    private String city;
    private String addressDetail;
    private String birthday;
    private String oldPassword;
    private String newPassword;
}
