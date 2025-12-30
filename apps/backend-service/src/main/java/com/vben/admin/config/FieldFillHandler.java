package com.vben.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.vben.admin.core.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 字段填充处理器
 *
 * @author vben
 */
@Component
public class FieldFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        // 填充创建人ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, "createBy", () -> currentUserId, String.class);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        // 填充更新人ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null) {
            this.strictUpdateFill(metaObject, "updateBy", () -> currentUserId, String.class);
        }
    }
}
