package com.vben.admin.util;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码生成工具（用于生成初始密码）
 *
 * 使用方法：
 * java -cp target/backend-service-1.0.0-SNAPSHOT.jar com.vben.admin.util.PasswordGenerator admin123
 *
 * @author vben
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String plainPassword = args.length > 0 ? args[0] : "admin123";
        String encodedPassword = encoder.encode(plainPassword);

        System.out.println("=========================================");
        System.out.println("密码生成工具");
        System.out.println("=========================================");
        System.out.println("原始密码: " + plainPassword);
        System.out.println("加密密码: " + encodedPassword);
        System.out.println("=========================================");

        // 验证密码
        boolean matches = encoder.matches(plainPassword, encodedPassword);
        System.out.println("密码验证: " + (matches ? "✅ 通过" : "❌ 失败"));
        System.out.println("=========================================");
        System.out.println();
        System.out.println("SQL 更新语句：");
        System.out.println("UPDATE sys_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
    }
}
