# Vben Admin 数据库初始化说明

## 📋 目录结构

- `schema.sql` - 数据库和表结构创建脚本
- `data.sql` - 初始数据插入脚本
- `init.sh` - 一键初始化脚本（Docker MySQL）

## 🚀 快速开始

### 方式 1：使用 Docker MySQL（推荐）

```bash
cd apps/backend-service

# 查找 MySQL 容器名称
docker ps | grep mysql

# 执行一键初始化脚本
./src/main/resources/db/init.sh

# 或者手动执行
MYSQL_CONTAINER="mysql-vben"  # 替换为实际容器名称
docker exec -i $MYSQL_CONTAINER mysql -uroot -proot < src/main/resources/db/schema.sql
docker exec -i $MYSQL_CONTAINER mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/data.sql
```

### 方式 2：使用本地 MySQL

```bash
cd apps/backend-service

# 1. 创建数据库和表结构
mysql -uroot -proot < src/main/resources/db/schema.sql

# 2. 初始化数据（使用 UTF-8 编码）
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/data.sql
```

### 方式 3：在 MySQL 客户端中执行

1. 连接到 MySQL 数据库
2. 执行 `schema.sql` 创建表结构
3. 执行 `data.sql` 初始化数据（确保客户端字符集为 UTF-8）

## 📝 脚本说明

### schema.sql

创建数据库和所有表结构，包括：

- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_menu` - 菜单表（使用 `sort_order` 字段，避免 MySQL 保留关键字冲突）
- `sys_dept` - 部门表
- `sys_user_role` - 用户角色关联表
- `sys_role_menu` - 角色菜单关联表
- `sys_permission` - 权限码表
- `sys_user_permission` - 用户权限关联表

**特点**：

- 所有表使用 `utf8mb4` 字符集
- 菜单表使用 `sort_order` 字段（非保留关键字）
- 支持逻辑删除（`deleted` 字段）
- 自动时间戳（`create_time`, `update_time`）
- 创建人和更新人追踪（`create_by`, `update_by`）

### data.sql

插入初始数据，包括：

- **部门**：总公司（用户需要关联部门）
- **管理员用户**：`admin` / `admin123`
  - 包含完整用户信息：真实姓名、昵称、手机号、性别、工号、部门等
- **角色**：`admin`（超级管理员）、`user`（普通用户）
- **菜单**：Dashboard、系统管理及其子菜单
- **权限码**：各种权限码定义

**特点**：

- 脚本开头设置 `SET NAMES utf8mb4`，确保中文正确插入
- 使用 `sort_order` 字段进行排序
- 所有中文数据使用 UTF-8 编码
- 部门数据在用户数据之前初始化（用户需要关联部门）

## ⚙️ 配置说明

### 字符编码

**重要**：执行 `data.sql` 时必须使用 UTF-8 编码，否则中文会乱码。

**Docker MySQL**：

```bash
docker exec -i <容器名称> mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < data.sql
```

**本地 MySQL**：

```bash
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < data.sql
```

**MySQL 客户端**：确保客户端字符集设置为 UTF-8，或在执行前运行：

```sql
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
```

### 数据库连接配置

应用配置（`application.yml`）：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/vben_admin?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

## ✅ 验证初始化

```sql
-- 检查表结构
SHOW TABLES;

-- 检查菜单表字段（确认 sort_order 存在）
SHOW COLUMNS FROM sys_menu WHERE Field = 'sort_order';

-- 检查数据
SELECT COUNT(*) FROM sys_user;  -- 应返回 1
SELECT COUNT(*) FROM sys_role;  -- 应返回 2
SELECT COUNT(*) FROM sys_menu; -- 应返回 6

-- 检查中文数据是否正确
SELECT id, name FROM sys_menu WHERE name LIKE '%管理%';
SELECT id, name, remark FROM sys_role;
```

## 🔑 默认账号

- **用户名**：`admin_user`
- **密码**：`admin123`
- **角色**：超级管理员（拥有所有权限）

**用户名规则**：

- 必须以小写字母开头
- 只能包含小写字母、数字和下划线
- 长度：4-20 个字符
- 必须唯一

## ⚠️ 注意事项

1. **字符编码**：执行 `data.sql` 时必须使用 UTF-8 编码，否则中文会乱码
2. **保留关键字**：菜单表使用 `sort_order` 而不是 `order`，避免 MySQL 保留关键字冲突
3. **逻辑删除**：所有表都支持逻辑删除，删除操作不会真正删除数据，只是标记 `deleted = 1`
4. **时间戳**：`create_time` 和 `update_time` 会自动填充，无需手动设置
5. **创建人和更新人**：`create_by` 和 `update_by` 字段会在插入和更新时自动填充当前登录用户ID

## 🔄 重新初始化

如果需要重新初始化数据库（删除所有表后重新创建）：

```bash
cd apps/backend-service

# 方式 1：使用 init.sh 脚本（推荐）
REINIT=true ./src/main/resources/db/init.sh

# 方式 2：手动执行
MYSQL_CONTAINER="mysql-vben"
docker exec -i $MYSQL_CONTAINER mysql -uroot -proot vben_admin <<EOF
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS sys_user_permission;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_dept;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;
EOF

# 然后重新执行 schema.sql 和 data.sql
docker exec -i $MYSQL_CONTAINER mysql -uroot -proot < src/main/resources/db/schema.sql
docker exec -i $MYSQL_CONTAINER mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/data.sql
```

## 📚 相关文档

- [数据库设计文档](../../README.md)
- [应用配置说明](../../src/main/resources/application.yml)
