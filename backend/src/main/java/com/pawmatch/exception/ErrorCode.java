package com.pawmatch.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 认证相关
    ACCOUNT_EXISTS(400, "账号已存在"),
    ACCOUNT_NOT_FOUND(404, "账号不存在"),
    PASSWORD_ERROR(401, "密码错误"),
    ACCOUNT_DISABLED(403, "账号已被禁用"),

    // 信息完善
    INFO_INCOMPLETE(403, "请先完善信息"),

    // 业务相关
    PET_NOT_FOUND(404, "宠物不存在"),
    PET_NOT_AVAILABLE(400, "该宠物不可领养"),
    DUPLICATE_APPLICATION(400, "您已提交过该宠物的领养申请"),
    APPLICATION_NOT_FOUND(404, "领养申请不存在"),
    APPLICATION_STATUS_ERROR(400, "申请状态不允许此操作"),

    SERVICE_NOT_FOUND(404, "寄养服务不存在"),
    ORDER_NOT_FOUND(404, "寄养订单不存在"),
    ORDER_STATUS_ERROR(400, "订单状态不允许此操作"),
    CAPACITY_INSUFFICIENT(400, "寄养容量不足"),

    POST_NOT_FOUND(404, "帖子不存在"),
    COMMENT_NOT_FOUND(404, "评论不存在"),

    NOTIFICATION_NOT_FOUND(404, "通知不存在"),
    PASSWORD_TOO_WEAK(400, "密码强度不足，至少8位且包含字母和数字");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
