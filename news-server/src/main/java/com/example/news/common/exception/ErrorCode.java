package com.example.news.common.exception;

/**
 * 业务异常码枚举
 */
public enum ErrorCode {

    // 通用错误
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // 用户相关 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "用户名或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被停用"),
    INVALID_TOKEN(1005, "无效的令牌"),
    TOKEN_EXPIRED(1006, "令牌已过期"),

    // 新闻相关 2xxx
    NEWS_NOT_FOUND(2001, "稿件不存在"),
    NEWS_STATUS_ERROR(2002, "稿件状态不允许此操作"),
    NEWS_PERMISSION_DENIED(2003, "无权操作此稿件"),

    // 分类相关 3xxx
    CATEGORY_NOT_FOUND(3001, "分类不存在"),
    CATEGORY_HAS_NEWS(3002, "该分类下存在稿件，无法删除"),

    // 评论相关 4xxx
    REVIEW_NOT_FOUND(4001, "评论不存在"),
    REVIEW_CONTENT_EMPTY(4002, "评论内容不能为空"),

    // 文件相关 5xxx
    FILE_UPLOAD_FAILED(5001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(5002, "不允许的文件类型"),
    FILE_TOO_LARGE(5003, "文件大小超出限制"),

    // 操作限制 6xxx
    RATE_LIMITED(6001, "操作过于频繁，请稍后重试"),
    DUPLICATE_OPERATION(6002, "请勿重复操作"),

    // 搜索相关 7xxx
    SEARCH_SERVICE_UNAVAILABLE(7001, "搜索服务暂不可用");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
