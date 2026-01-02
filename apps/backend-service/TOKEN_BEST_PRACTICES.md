# JWT Token 过期时间最佳实践

## 业界标准建议

### AccessToken 过期时间

**推荐值：15 分钟 - 2 小时**

- **高安全要求**：5-15 分钟（银行、支付系统）
- **一般系统**：30 分钟 - 2 小时（推荐）
- **低安全要求**：4-8 小时（内部系统）

**当前配置**：2 小时（7200000 毫秒）✅ **符合业界标准**

### RefreshToken 过期时间

**推荐值：7-30 天**

- **高安全要求**：7 天
- **一般系统**：15-30 天（推荐）
- **用户体验优先**：30-90 天（需要配合其他安全措施）

**当前配置**：7 天（604800000 毫秒）✅ **符合业界标准**

## Cookie 名称最佳实践

### 当前问题

使用 `jwt` 作为 Cookie 名称**不够语义化**，存在以下问题：

1. **不够明确**：`jwt` 可能被误解为 accessToken 或其他 token
2. **不符合命名规范**：应该使用更描述性的名称
3. **不利于调试**：在浏览器开发者工具中不够直观

### 推荐命名

业界常见的命名方式：

1. **`refreshToken`** - 最直观，明确表示是刷新令牌
2. **`refresh_token`** - 使用下划线分隔（部分团队偏好）
3. **`rt`** - 简短形式（refresh token 的缩写）
4. **`app_refresh_token`** - 带应用前缀，避免与其他系统冲突

**推荐使用**：`refreshToken` 或 `refresh_token`

## 安全建议

### 1. Token 过期时间平衡

```
AccessToken: 15分钟 - 2小时
  ↓ (过期后自动刷新)
RefreshToken: 7-30天
  ↓ (过期后需要重新登录)
用户重新登录
```

### 2. Cookie 安全属性

- ✅ **HttpOnly**: 防止 XSS 攻击（已实现）
- ✅ **Secure**: 生产环境必须开启（HTTPS）
- ✅ **SameSite**: 防止 CSRF 攻击
  - `Strict`: 最安全，但可能影响跨站请求
  - `Lax`: 平衡安全性和可用性（推荐）
  - `None`: 仅用于跨域场景，必须配合 `Secure`

### 3. Token 轮换（Token Rotation）

**当前实现**：刷新后继续使用原 refreshToken（简单模式）

**更安全的实现**：每次刷新时生成新的 refreshToken，并撤销旧的（需要后端支持）

## 配置建议

### 生产环境推荐配置

```yaml
jwt:
  access-token-expiration: 900000    # 15分钟（高安全）或 3600000（1小时，平衡）
  refresh-token-expiration: 2592000000  # 30天（604800000 = 7天，更安全）
```

### Cookie 名称建议

```java
// 推荐：使用更语义化的名称
private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
// 或
private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
```

## 参考标准

- **OWASP**: AccessToken 建议 15 分钟，RefreshToken 建议 7-30 天
- **Auth0**: AccessToken 默认 24 小时，RefreshToken 默认 30 天
- **AWS Cognito**: AccessToken 默认 1 小时，RefreshToken 默认 30 天
- **Google OAuth**: AccessToken 默认 1 小时，RefreshToken 可长期有效

## 总结

| 项目 | 当前值 | 业界推荐 | 建议 |
|------|--------|----------|------|
| AccessToken | 2 小时 | 15分钟-2小时 | ✅ 符合标准 |
| RefreshToken | 7 天 | 7-30 天 | ✅ 符合标准 |
| Cookie 名称 | `jwt` | `refreshToken` | ⚠️ 建议改进 |
