package com.juzi.chien.admin.common;

/**
 * 业务错误码枚举
 * 规则：5位数，前2位表示模块，后3位表示具体错误
 */
public enum ErrorCode {

    // 通用 10xxx
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "权限不足，拒绝访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户模块 20xxx
    USER_NOT_FOUND(20001, "用户不存在"),
    USER_ALREADY_EXISTS(20002, "用户名已存在"),
    USER_PASSWORD_ERROR(20003, "用户名或密码错误"),
    USER_DISABLED(20004, "用户已被停用"),
    USER_NOT_LOGIN(20005, "用户未登录"),

    // 角色模块 30xxx
    ROLE_NOT_FOUND(30001, "角色不存在"),
    ROLE_ALREADY_EXISTS(30002, "角色标识已存在"),
    ROLE_IN_USE(30003, "该角色下存在用户，无法删除"),

    // 菜单模块 40xxx
    MENU_NOT_FOUND(40001, "菜单不存在"),
    MENU_HAS_CHILDREN(40002, "该菜单下存在子菜单，无法删除"),

    // 字典模块 50xxx
    DICT_TYPE_NOT_FOUND(50001, "字典类型不存在"),
    DICT_TYPE_ALREADY_EXISTS(50002, "字典类型已存在"),
    DICT_TYPE_IN_USE(50003, "该字典类型下存在数据，请先删除数据"),

    // 文件模块 60xxx
    FILE_UPLOAD_ERROR(60001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(60002, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(60003, "文件大小超出限制");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
