# 数据库清理说明

## 清理方式

### 方式 1: 使用 MySQL 命令行（推荐）

如果您的系统已安装 MySQL 客户端，可以直接执行：

```bash
cd apps/backend-service/src/main/resources/db
mysql -uroot -proot vben_admin < clean-all.sql
```

### 方式 2: 在 MySQL 客户端中执行

1. 连接到 MySQL 数据库
2. 选择数据库：`USE vben_admin;`
3. 执行清理脚本：`source clean-all.sql;`

### 方式 3: 复制 SQL 内容手动执行

打开 `clean-all.sql` 文件，复制所有内容，在 MySQL 客户端中粘贴执行。

## 清理内容

清理脚本会删除以下所有表：

- `sys_user_permission` - 用户权限关联表
- `sys_role_menu` - 角色菜单关联表
- `sys_user_role` - 用户角色关联表
- `sys_operation_log` - 操作日志表
- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_menu` - 菜单表
- `sys_dept` - 部门表
- `sys_permission` - 权限码表

## 清理后重新初始化

清理完成后，可以运行以下命令重新初始化数据库：

```bash
cd apps/backend-service/src/main/resources/db
./scripts/init-all.sh
```

## 注意事项

- ⚠️ **此操作不可逆**：清理会删除所有表和数据
- ✅ **幂等性**：可以多次执行，不会报错
- ✅ **安全**：使用 `DROP TABLE IF EXISTS`，表不存在也不会报错
