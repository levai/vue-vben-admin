# Vben Admin 数据库初始化说明

## 📋 目录结构

```
apps/backend-service/src/main/resources/db/
├── README.md                    # 数据库脚本说明文档（本文件）
├── init.sql                     # 主入口文件（创建数据库、设置字符集）
├── clean-all.sql                # 清理所有表的脚本
├── modules/                     # 模块目录
│   ├── user/                    # 用户模块
│   │   ├── user-schema.sql     # 用户表结构
│   │   └── user-data.sql       # 用户初始数据
│   ├── role/                    # 角色模块
│   │   ├── role-schema.sql     # 角色表结构
│   │   └── role-data.sql       # 角色初始数据
│   ├── menu/                    # 菜单模块
│   │   ├── menu-schema.sql     # 菜单表结构
│   │   └── menu-data.sql       # 菜单初始数据
│   ├── dept/                    # 部门模块
│   │   ├── dept-schema.sql     # 部门表结构
│   │   └── dept-data.sql       # 部门初始数据
│   ├── permission/              # 权限模块
│   │   ├── permission-schema.sql    # 权限表结构
│   │   └── permission-data.sql      # 权限初始数据
│   ├── operation-log/           # 操作日志模块
│   │   ├── operation-log-schema.sql # 操作日志表结构
│   │   └── operation-log-data.sql   # 操作日志初始数据（可选）
│   └── relation/                 # 关联关系模块
│       ├── relation-schema.sql      # 关联表结构（user_role, role_menu, user_permission）
│       └── relation-data.sql        # 关联关系初始数据
└── scripts/                      # 工具脚本目录
    └── init-all.sh               # 执行所有模块的初始化脚本（推荐使用）
```

## 🚀 快速开始

### 方式 1：使用模块化脚本（推荐）⭐

**使用 Docker MySQL**：

```bash
cd apps/backend-service

# 使用默认配置
./src/main/resources/db/scripts/init-all.sh

# 或指定 Docker 容器名称
./src/main/resources/db/scripts/init-all.sh -c mysql-vben

# 或使用环境变量
MYSQL_CONTAINER=mysql-vben ./src/main/resources/db/scripts/init-all.sh
```

**使用本地 MySQL**：

```bash
cd apps/backend-service

# 使用本地 MySQL
./src/main/resources/db/scripts/init-all.sh --no-docker -u root -p yourpassword

# 或指定主机和端口
./src/main/resources/db/scripts/init-all.sh --no-docker -h localhost -P 3306 -u root -p yourpassword
```

### 方式 2：手动执行模块文件

如果需要单独执行某个模块：

```bash
# 1. 先执行 init.sql 创建数据库
mysql -uroot -proot < src/main/resources/db/init.sql

# 2. 按顺序执行各模块（注意依赖关系）
# 基础模块
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/modules/dept/dept-schema.sql
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/modules/dept/dept-data.sql

mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/modules/role/role-schema.sql
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/modules/role/role-data.sql

# ... 其他模块
```

## 📝 模块说明

### 模块划分

1. **部门模块 (dept)**
   - 表：`sys_dept`
   - 初始数据：总公司

2. **角色模块 (role)**
   - 表：`sys_role`
   - 初始数据：admin、user 角色

3. **菜单模块 (menu)**
   - 表：`sys_menu`
   - 初始数据：Dashboard、系统管理及其子菜单、按钮权限

4. **权限模块 (permission)**
   - 表：`sys_permission`
   - 初始数据：所有权限码定义

5. **用户模块 (user)**
   - 表：`sys_user`
   - 初始数据：管理员用户
   - **依赖**：部门模块

6. **关联关系模块 (relation)**
   - 表：`sys_user_role`、`sys_role_menu`、`sys_user_permission`
   - 初始数据：用户角色关联、角色菜单关联、用户权限关联
   - **依赖**：用户、角色、菜单、权限模块

7. **操作日志模块 (operation-log)**
   - 表：`sys_operation_log`
   - 初始数据：通常不需要
   - **依赖**：用户模块

### 执行顺序

由于存在依赖关系，必须按以下顺序执行：

1. **基础模块**（无依赖）：
   - dept（部门）
   - role（角色）
   - menu（菜单）
   - permission（权限码）

2. **业务模块**（依赖基础模块）：
   - user（用户）- 依赖 dept

3. **关联模块**（依赖所有基础模块）：
   - relation（关联关系）- 依赖 user、role、menu、permission

4. **日志模块**（独立）：
   - operation-log（操作日志）- 依赖 user

## ⚙️ 配置说明

### 字符编码

**重要**：执行所有 SQL 文件时必须使用 UTF-8 编码，否则中文会乱码。

**Docker MySQL**：

```bash
docker exec -i <容器名称> mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < <文件路径>
```

**本地 MySQL**：

```bash
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < <文件路径>
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
      url: jdbc:mysql://localhost:3306/vben_admin?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

## ✅ 验证初始化

```sql
-- 检查表结构
SHOW TABLES;

-- 检查各模块表
SELECT COUNT(*) FROM sys_user;      -- 应返回 1
SELECT COUNT(*) FROM sys_role;      -- 应返回 2
SELECT COUNT(*) FROM sys_menu;      -- 应返回多个
SELECT COUNT(*) FROM sys_dept;      -- 应返回 1
SELECT COUNT(*) FROM sys_permission; -- 应返回多个

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

1. **字符编码**：执行所有 SQL 文件时必须使用 UTF-8 编码，否则中文会乱码
2. **保留关键字**：菜单表使用 `sort_order` 而不是 `order`，避免 MySQL 保留关键字冲突
3. **逻辑删除**：所有表都支持逻辑删除，删除操作不会真正删除数据，只是标记 `deleted = 1`
4. **时间戳**：`create_time` 和 `update_time` 会自动填充，无需手动设置
5. **创建人和更新人**：`create_by` 和 `update_by` 字段会在插入和更新时自动填充当前登录用户ID
6. **执行顺序**：必须按照依赖关系顺序执行各模块
7. **幂等性**：所有 SQL 文件使用 `CREATE TABLE IF NOT EXISTS` 和 `INSERT ... ON DUPLICATE KEY UPDATE`，支持重复执行

## 🔄 重新初始化

如果需要重新初始化数据库（删除所有表后重新创建）：

```bash
cd apps/backend-service

# 方式 1：使用模块化脚本（推荐）
MYSQL_CONTAINER=mysql-vben ./src/main/resources/db/scripts/init-all.sh

# 方式 2：使用清理脚本后重新执行
# 先清理数据库
./src/main/resources/db/scripts/clean-all.sh --no-docker -u root -p root

# 然后重新执行初始化脚本
./src/main/resources/db/scripts/init-all.sh --no-docker -u root -p root
```

## 📚 模块化优势

1. **模块化**：每个模块独立，便于维护
2. **可扩展**：新增模块只需添加新目录和文件
3. **清晰**：模块职责明确，依赖关系清晰
4. **灵活**：可以单独执行某个模块的初始化
5. **版本控制**：便于跟踪各模块的变更历史

## 🔧 单独执行某个模块

如果需要单独执行某个模块（例如只更新菜单数据）：

```bash
# 只执行菜单模块
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/modules/menu/menu-schema.sql
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/modules/menu/menu-data.sql
```

## 📚 相关文档

- [数据库设计文档](../../README.md)
- [应用配置说明](../../src/main/resources/application.yml)
- [后端开发规范](../../../../.cursor/rules/backend-development.mdc)
