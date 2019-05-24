package com.fukun.commons.web.constants;

import com.fukun.commons.enums.ApiStyleEnum;
import com.fukun.commons.enums.CallSourceEnum;

/**
 * Header的key罗列
 *
 * @author tangyifei
 * @since 2019-5-23 17:10:03 PM
 */
public class HeaderConstants {

    /**
     * 用户的登录token
     */
    public static final String X_TOKEN = "X-Token";

    /**
     * api的版本号
     */
    public static final String API_VERSION = "Api-Version";

    /**
     * app版本号
     */
    public static final String APP_VERSION = "App-Version";

    /**
     * 调用来源 {@link CallSourceEnum}
     */
    public static final String CALL_SOURCE = "Call-Source";

    /**
     * API的返回格式 {@link ApiStyleEnum}
     */
    public static final String API_STYLE = "Api-Style";
}
