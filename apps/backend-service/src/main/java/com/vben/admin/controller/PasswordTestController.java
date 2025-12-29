package com.vben.admin.controller;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 密码生成测试 Controller（仅用于开发环境）
 *
 * @author vben
 */
@RestController
@RequestMapping("/test")
public class PasswordTestController {

    @GetMapping("/generate-password")
    public Map<String, Object> generatePassword(@RequestParam(defaultValue = "admin123") String password) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        boolean matches = encoder.matches(password, encodedPassword);

        Map<String, Object> result = new HashMap<>();
        result.put("plainPassword", password);
        result.put("encodedPassword", encodedPassword);
        result.put("matches", matches);
        result.put("sql", "UPDATE sys_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");

        return result;
    }
}
