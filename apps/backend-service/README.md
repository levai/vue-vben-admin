# Vben Admin 后端服务

基于 Spring Boot 2.7.18 + Java 11 的后端服务，提供完整的用户、角色、菜单、部门管理功能。

## 📋 技术栈

- **框架**: Spring Boot 2.7.18
- **Java**: 11
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis Plus 3.5.9
- **安全**: Spring Security + JWT
- **API 文档**: Knife4j (Swagger)
- **连接池**: Druid
- **构建工具**: Maven

## 🚀 快速开始

### 1. 环境要求

- Java 11+
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
    druid:
      url: jdbc:mysql://localhost:3306/vben_admin?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      username: root
      password: root
```

### 4. 启动应用

#### 前台启动（开发调试）

```bash
cd apps/backend-service
JAVA_HOME=/path/to/java11
JAVA_HOME=$JAVA_HOME mvn spring-boot:run
```

#### 后台启动（生产环境）

```bash
cd apps/backend-service
JAVA_HOME=/path/to/java11
nohup JAVA_HOME=$JAVA_HOME mvn spring-boot:run > target/app.log 2>&1 &
echo $! > target/app.pid
```

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
- `GET /system/user/list` - 获取用户列表（支持分页和搜索）
- `GET /system/user/{id}` - 获取用户详细信息
- `POST /system/user` - 创建用户
- `PUT /system/user/{id}` - 更新用户信息
- `DELETE /system/user/{id}` - 删除用户（逻辑删除）
- `PUT /system/user/{id}/status` - 启用/禁用用户
- `PUT /system/user/{id}/password` - 重置用户密码

### 菜单相关 (`/menu/*`, `/system/menu/*`)

- `GET /menu/all` - 获取所有菜单（用于路由）
- `GET /system/menu/list` - 获取菜单列表
- `GET /system/menu/name-exists` - 检查菜单名称是否存在
- `GET /system/menu/path-exists` - 检查菜单路径是否存在
- `POST /system/menu` - 创建菜单
- `PUT /system/menu/{id}` - 更新菜单
- `DELETE /system/menu/{id}` - 删除菜单

### 角色相关 (`/system/role/*`)

- `GET /system/role/list` - 获取角色列表（支持分页）
- `POST /system/role` - 创建角色
- `PUT /system/role/{id}` - 更新角色
- `DELETE /system/role/{id}` - 删除角色

### 部门相关 (`/system/dept/*`)

- `GET /system/dept/list` - 获取部门列表
- `POST /system/dept` - 创建部门
- `PUT /system/dept/{id}` - 更新部门
- `DELETE /system/dept/{id}` - 删除部门

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

- 数据库连接：`characterEncoding=utf8mb4`
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

- [启动说明](启动说明.md)
- [数据库初始化说明](src/main/resources/db/README.md)

## 🐛 常见问题

### 1. 端口被占用

```bash
# 查找占用 8080 端口的进程
lsof -ti:8080

# 停止进程
kill $(lsof -ti:8080)
```

### 2. 数据库连接失败

检查：

- MySQL 是否启动
- 数据库连接配置是否正确
- 数据库是否已创建

### 3. 中文乱码

确保：

- 数据库字符集为 `utf8mb4`
- 执行 `data.sql` 时使用 `--default-character-set=utf8mb4`
- 应用配置中字符编码设置正确

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

## 📄 License

MIT
