package com.vben.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * 后端服务启动类
 *
 * @author vben
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackages = {"com.vben.admin.mapper"})
@ComponentScan(basePackages = {"com.vben.admin"})
public class BackendServiceApplication {

    public static void main(String[] args) {
        // 设置应用默认时区为 Asia/Shanghai
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(BackendServiceApplication.class, args);
    }

    @PostConstruct
    void started() {
        // 确保应用启动后时区为 Asia/Shanghai
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}
