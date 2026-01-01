package com.vben.admin.core.task;

import com.vben.admin.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志清理任务
 * 每天凌晨2点执行，清理3个月前的日志
 *
 * @author vben
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogCleanupTask {

    private final OperationLogService operationLogService;

    /**
     * 清理过期日志
     * cron表达式：秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredLogs() {
        log.info("开始清理过期操作日志...");
        try {
            // 删除3个月前的日志
            LocalDateTime expireTime = LocalDateTime.now().minusMonths(3);
            int count = operationLogService.cleanExpiredLogs(expireTime);
            log.info("清理完成，共删除 {} 条日志", count);
        } catch (Exception e) {
            log.error("清理过期操作日志失败", e);
        }
    }
}
