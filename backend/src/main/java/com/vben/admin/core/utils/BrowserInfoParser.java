package com.vben.admin.core.utils;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 浏览器信息解析工具
 *
 * @author vben
 */
public class BrowserInfoParser {

    /**
     * 浏览器信息
     */
    @Data
    public static class BrowserInfo {
        /**
         * 浏览器类型
         */
        private String browser;
        /**
         * 操作系统
         */
        private String os;
    }

    /**
     * 解析User-Agent获取浏览器和操作系统信息
     *
     * @param userAgent User-Agent字符串
     * @return 浏览器信息
     */
    public static BrowserInfo parseUserAgent(String userAgent) {
        BrowserInfo info = new BrowserInfo();

        if (!StringUtils.hasText(userAgent)) {
            return info;
        }

        String ua = userAgent.toLowerCase();

        // 解析浏览器类型
        if (ua.contains("edg")) {
            info.setBrowser("Edge");
        } else if (ua.contains("chrome")) {
            info.setBrowser("Chrome");
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            info.setBrowser("Safari");
        } else if (ua.contains("firefox")) {
            info.setBrowser("Firefox");
        } else if (ua.contains("opera") || ua.contains("opr")) {
            info.setBrowser("Opera");
        } else if (ua.contains("msie") || ua.contains("trident")) {
            info.setBrowser("IE");
        } else {
            info.setBrowser("Unknown");
        }

        // 解析操作系统
        if (ua.contains("windows")) {
            if (ua.contains("windows nt 10.0")) {
                info.setOs("Windows 10");
            } else if (ua.contains("windows nt 6.3")) {
                info.setOs("Windows 8.1");
            } else if (ua.contains("windows nt 6.2")) {
                info.setOs("Windows 8");
            } else if (ua.contains("windows nt 6.1")) {
                info.setOs("Windows 7");
            } else {
                info.setOs("Windows");
            }
        } else if (ua.contains("mac")) {
            info.setOs("Mac OS");
        } else if (ua.contains("linux")) {
            info.setOs("Linux");
        } else if (ua.contains("android")) {
            info.setOs("Android");
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            info.setOs("iOS");
        } else {
            info.setOs("Unknown");
        }

        return info;
    }
}
