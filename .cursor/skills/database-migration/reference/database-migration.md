---
description: 规范化管理后端数据库结构的变更流程。
---

# 数据库迁移工作流

此工作流用于指导如何安全地对数据库进行 Schema 变更，确保开发与生产环境的一致性。

## 必备条件

- 访问数据库管理工具或 CLI。
- 了解项目使用的迁移工具（如 Flyway, Liquibase 或原始 SQL 脚本）。

## 路径与命名

- **Entity 位置**：`backend/src/main/java/com/vben/admin/model/entity/`（表对应 `SysXxx.java`）。
- **SQL 脚本**：项目约定命名如 `V202401201200__Add_User_Phone.sql`（幂等、可重复执行）。

## 工作流步骤

1. **方案评审**：明确变更内容（新增列、修改索引等）及其对现有数据的影响。
2. **脚本编写**：生成符合规范的 SQL 迁移脚本，保持幂等；复杂变更提供回滚说明。
3. **本地测试**：在本地库运行并验证。
4. **实体更新**：根据库表同步更新 **`backend/.../model/entity/`** 下对应 Entity 类。
5. **提交归档**：脚本提交至版本控制指定目录（如 `backend/src/main/resources/db/` 或项目约定目录）。

## 最佳实践

- **应当 (DO)**: 保持脚本的幂等性，避免重复执行报错。
- **应当 (DO)**: 为复杂的变更提供回滚（Rollback）脚本。
- **避免 (DON'T)**: 直接在生产环境手动执行未经版本控制的 SQL 语句。

## 示例

**提示词:**

> 为用户表新增一个"手机号 (phone_number)"字段，并更新相关实体类。

**SQL 示例:**

```sql
-- V202401201200__Add_User_Phone.sql
ALTER TABLE `sys_user` ADD COLUMN `phone_number` VARCHAR(20) DEFAULT NULL COMMENT '手机号' AFTER `email`;
```
