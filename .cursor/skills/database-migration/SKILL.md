---
name: database-migration
description: 规范化后端数据库 Schema 变更流程。在用户要求做数据库迁移、加字段、改表结构、SQL 脚本、同步 Entity 时使用。
---

# 数据库迁移

**完整工作流**：[reference/database-migration.md](reference/database-migration.md)

## 何时使用

- 数据库 Schema 变更、新增列、修改索引、建表；需迁移脚本并同步 Entity

## 步骤要点

1. **方案**：明确变更内容及对现有数据的影响。
2. **脚本**：生成符合规范的 SQL（如 `V202401201200__Add_User_Phone.sql`）；保持幂等，复杂变更提供回滚。
3. **本地测试**：运行并验证。
4. **实体**：根据库表同步更新 **`backend/src/main/java/com/vben/admin/model/entity/`** 下对应 Entity。
5. **归档**：脚本提交版本控制指定目录。

**避免**：在生产直接执行未经版本控制的 SQL。
