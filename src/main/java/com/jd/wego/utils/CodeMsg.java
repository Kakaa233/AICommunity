package com.jd.wego.utils;

import lombok.Data;

/**
 * @author hbquan
 * @date 2021/3/30 15:59
 */
@Data
public class CodeMsg {

    private int code;
    private String msg;

    public CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 通用错误
     */
    public static CodeMsg SUCCESS = new CodeMsg(0, "SUCCESS");
    public static CodeMsg ERROR = new CodeMsg(000000, "ERROR");
    public static CodeMsg NOT_LOGIN = new CodeMsg(000001, "NOT_LOGIN");

    /**
     * 登录注册模块错误
     */
    public static CodeMsg VERIFY_CODE_ERROR = new CodeMsg(100000, "验证码错误");
    public static CodeMsg DUPLICATE_REGISTRY = new CodeMsg(100001, "账号已注册");
    public static CodeMsg UNREGISTER_PHONE = new CodeMsg(100002, "账号名未注册");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(100003, "登录密码错误");
    public static CodeMsg GITHUB_CODE_OR_STATE_EMPTY = new CodeMsg(100004, "Github获取code或者state为null");
    public static CodeMsg GITHUB_REQUEST_TOKEN_EMPTY = new CodeMsg(100005, "Github请求获取Token失败");
    public static CodeMsg GITHUB_REQUEST_USER_INFO_EMPTY = new CodeMsg(100006, "Github请求获取用户信息失败");
    public static CodeMsg FORMAT_ERROR = new CodeMsg(100007, "格式不符合要求");


    /**
     * 个人中心模块错误
     */
    public static CodeMsg UPLOAD_IMAGE_EMPTY = new CodeMsg(200000, "上传图片为空");
}
