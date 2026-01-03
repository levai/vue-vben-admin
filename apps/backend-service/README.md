# Vben Admin 后端服务

基于 Spring Boot 3.2.0 + Java 17 的后端服务，提供完整的用户、角色、菜单、部门管理功能。

## 📋 技术栈

- **框架**: Spring Boot 3.2.0
- **Java**: 17 (LTS)
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis Plus 3.5.7 (Spring Boot 3 专用版本)
- **安全**: Spring Security 3.2.0 + JWT (jjwt 0.12.5)
- **API 文档**: Knife4j 4.4.0 (SpringDoc OpenAPI 3)
- **连接池**: Druid 1.2.23
- **构建工具**: Maven 3.6+

## 🚀 快速开始

### 1. 环境要求

- Java 17+ (推荐使用 jenv 管理 Java 版本，项目包含 `.java-version` 文件)
- Maven 3.6+
- MySQL 8.0+（或 Docker MySQL）

### 2. 数据库初始化

#### Docker MySQL（推荐）

```bash
cd apps/backend-service
./src/main/resources/db/init.sh
```

#### 本地 MySQL

```bash
cd apps/backend-service
mysql -uroot -proot < src/main/resources/db/schema.sql
mysql -uroot -proot --default-character-set=utf8mb4 vben_admin < src/main/resources/db/data.sql
```

详细说明请参考：[数据库初始化文档](src/main/resources/db/README.md)

### 3. 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vben_admin?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
    druid:
      # Druid 连接池配置
      initial-size: 5
      min-idle: 5
      max-active: 20
```

### 4. 启动应用

#### 使用 pnpm 启动（推荐）

```bash
cd apps/backend-service
eval "$(jenv init -)"  # 初始化 jenv（自动读取 .java-version）
pnpm run dev
```

#### 直接使用 Maven 启动

```bash
cd apps/backend-service
eval "$(jenv init -)"  # 初始化 jenv（自动读取 .java-version）
mvn spring-boot:run
```

**注意**：

- 项目使用 `.java-version` 文件配合 jenv 管理 Java 版本
- 启动前会自动清理 8080 端口（如果被占用）
- 确保 jenv 中已配置 Java 17：`jenv add /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`

详细启动说明请参考：[启动说明文档](启动说明.md)

### 5. 验证启动

应用启动后访问：

- API 文档: http://localhost:8080/doc.html
- 健康检查: http://localhost:8080/actuator/health

## 🔑 默认账号

- **用户名**: `admin`
- **密码**: `admin123`

## 📚 API 接口

### 认证相关 (`/auth/*`)

- `POST /auth/login` - 登录
- `POST /auth/logout` - 退出登录
- `POST /auth/refresh` - 刷新 Token
- `GET /auth/codes` - 获取权限码列表

### 用户相关 (`/user/*`, `/system/user/*`)

- `GET /user/info` - 获取当前登录用户信息
- `GET /system/user` - 获取用户列表（支持分页和搜索）
- `GET /system/user/{id}` - 获取用户详细信息
- `POST /system/user` - 创建用户
- `PUT /system/user/{id}` - 更新用户信息
- `DELETE /system/user/{id}` - 删除用户（逻辑删除）
- `PUT /system/user/{id}/status` - 启用/禁用用户
- `PUT /system/user/{id}/password` - 重置用户密码
- `GET /system/user/options` - 获取用户选项列表（用于下拉选项）

### 菜单相关 (`/menu/*`, `/system/menu/*`)

- `GET /menu/all` - 获取所有菜单（用于路由）
- `GET /system/menu` - 获取菜单列表（树形结构）
- `GET /system/menu/{id}` - 获取菜单详细信息
- `GET /system/menu/name-exists` - 检查菜单名称是否存在
- `GET /system/menu/path-exists` - 检查菜单路径是否存在
- `POST /system/menu` - 创建菜单
- `PUT /system/menu/{id}` - 更新菜单
- `DELETE /system/menu/{id}` - 删除菜单
- `PUT /system/menu/batch-order` - 批量更新菜单排序

### 角色相关 (`/system/role/*`)

- `GET /system/role` - 获取角色列表（支持分页）
- `POST /system/role` - 创建角色
- `PUT /system/role/{id}` - 更新角色
- `DELETE /system/role/{id}` - 删除角色

### 部门相关 (`/system/dept/*`)

- `GET /system/dept` - 获取部门列表（树形结构）
- `GET /system/dept/{id}` - 获取部门详细信息
- `POST /system/dept` - 创建部门
- `PUT /system/dept/{id}` - 更新部门
- `DELETE /system/dept/{id}` - 删除部门

### 操作日志相关 (`/system/operation-log/*`)

- `GET /system/operation-log` - 获取操作日志列表（支持分页和筛选）
- `GET /system/operation-log/{id}` - 获取操作日志详细信息
- `DELETE /system/operation-log/{id}` - 删除操作日志
- `DELETE /system/operation-log/batch` - 批量删除操作日志
- `GET /system/operation-log/types` - 获取操作类型列表（用于下拉选项）
- `GET /system/operation-log/modules` - 获取操作模块列表（用于下拉选项）

## 🏗️ 项目结构

```
apps/backend-service/
├── src/main/java/com/vben/admin/
│   ├── BackendServiceApplication.java  # 启动类
│   ├── config/                         # 配置类
│   │   ├── SecurityConfiguration.java  # Spring Security 配置
│   │   ├── MybatisPlusConfig.java      # MyBatis Plus 配置
│   │   ├── Knife4jConfig.java          # Swagger 配置
│   │   └── ...
│   ├── controller/                     # 控制器
│   ├── service/                        # 服务层
│   ├── mapper/                         # Mapper 接口
│   ├── model/                          # 数据模型
│   │   ├── entity/                     # 实体类
│   │   ├── dto/                        # 数据传输对象
│   │   └── vo/                         # 视图对象
│   └── core/                           # 核心功能
│       ├── enums/                      # 枚举类
│       ├── exception/                  # 异常处理
│       ├── filter/                     # 过滤器
│       ├── model/                      # 通用模型
│       └── utils/                      # 工具类
├── src/main/resources/
│   ├── application.yml                 # 应用配置
│   ├── db/                             # 数据库脚本
│   │   ├── schema.sql                  # 表结构
│   │   ├── data.sql                    # 初始数据
│   │   ├── init.sh                     # 初始化脚本
│   │   └── README.md                   # 数据库文档
│   └── mapper/                         # MyBatis XML
└── pom.xml                             # Maven 配置
```

## 🔧 配置说明

### 字符编码

项目已配置 UTF-8 编码：

- 数据库连接：`characterEncoding=UTF-8`（注意：Spring Boot 3 使用 `UTF-8` 而不是 `utf8mb4`）
- JVM 参数：`-Dfile.encoding=UTF-8`
- Tomcat：`uri-encoding: UTF-8`

### 逻辑删除

所有表都支持逻辑删除，使用 `deleted` 字段：

- `0` - 未删除
- `1` - 已删除

### 自动时间戳

所有表都有 `create_time` 和 `update_time` 字段，自动填充：

- `create_time` - 创建时间（插入时自动填充）
- `update_time` - 更新时间（更新时自动更新）

### MySQL 保留关键字

菜单表使用 `sort_order` 字段而不是 `order`，避免 MySQL 保留关键字冲突。

## 📖 相关文档

### 技术栈与开发规范

- **[技术栈与开发规范分析](docs/技术栈与开发规范分析.md)** - 详细的技术栈分析、代码规范和开发模式
  - 技术栈版本说明（Spring Boot 3.2.0 + Java 17）
  - 代码规范总结
  - 架构设计模式
  - 后续开发模式
  - 最佳实践建议

### 最佳实践

- **[Token 最佳实践](docs/TOKEN_BEST_PRACTICES.md)** - JWT Token 使用最佳实践
  - Token 设计原则
  - 安全建议
  - 实现示例

### 数据库

- **[数据库初始化说明](src/main/resources/db/README.md)** - 数据库初始化指南

> 💡 **提示**：更多详细文档请查看 [`docs/`](docs/) 目录

## 🐛 常见问题

### 1. 端口被占用

项目启动脚本已自动处理端口清理，如果手动启动遇到端口占用：

```bash
# 查找占用 8080 端口的进程
lsof -ti:8080

# 停止进程
kill $(lsof -ti:8080)

# 或者使用强制清理（推荐）
lsof -ti:8080 | xargs kill -9 2>/dev/null
```

**注意**：使用 `pnpm run dev` 启动时，会自动清理 8080 端口。

### 2. 数据库连接失败

检查：

- MySQL 是否启动
- 数据库连接配置是否正确
- 数据库是否已创建

### 3. 中文乱码

确保：

- 数据库字符集为 `utf8mb4`
- 执行 `data.sql` 时使用 `--default-character-set=utf8mb4`
- 应用配置中字符编码设置为 `UTF-8`（注意：Spring Boot 3 使用 `UTF-8` 而不是 `utf8mb4`）

### 4. 登录失败

检查：

- 数据库中的密码是否正确（BCrypt 加密）
- 用户名和密码是否匹配
- 用户是否被逻辑删除（`deleted = 0`）

## 📝 开发规范

- 统一使用 UTF-8 编码
- 所有表支持逻辑删除
- 统一响应格式：`{ code: 0, data: T, message: "ok" }`
- 使用 MyBatis Plus 进行数据库操作
- 使用 JWT 进行身份认证
- 使用 `.java-version` 文件配合 jenv 管理 Java 版本
- 所有 `javax.*` 包已迁移到 `jakarta.*`（Spring Boot 3 要求）

## 📄 License

MIT
