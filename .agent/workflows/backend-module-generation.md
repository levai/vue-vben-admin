---
description: 在 Spring Boot 架构下生成新的后端功能模块，包括 Controller, Service, Mapper 和 Model。
---

# 后端模块生成工作流

此工作流指导 Agent 在 `backend` 项目中创建符合规范的功能模块代码。

## 必备条件

- Java 17+ 和 Spring Boot 环境。
- 熟悉 MyBatis-Plus 或类似的 ORM 框架架构。
- 对应模块的包路径（通常为 `com.vben.admin` 下）。

## 工作流步骤

1.  **包路径确认**: 确定新模块在 `controller`, `service`, `mapper`, `model` 下的子包。
2.  **Model 定义**: 创建实体类（Entity）和请求/响应 DTO。
3.  **Mapper 实现**: 创建 Mapper 接口及相应的 XML 文件（如果需要）。
4.  **Service 开发**: 定义 Service 接口并实现业务逻辑。
5.  **Controller 暴露**: 编写 RESTful 风格的 Controller 接口。
6.  **代码清理**: 优化 Import，确保符合代码规范。

## 最佳实践

- **应当 (DO)**: 使用 `@RestController` 和 `@RequestMapping` 定义接口。
- **应当 (DO)**: 为公共接口标注 Swagger/OpenAPI 注解。
- **应当 (DO)**: **Spring Boot 3 使用 `jakarta.validation` 而不是 `javax.validation`**（重要！）
- **应当 (DO)**: 分页接口返回的数据字段必须统一为 `list` (数组) 和 `total` (总数)，严禁使用 `items` 或 `data` 作为数组字段名。
- **避免 (DON'T)**: 在 Service 层之外引用 Mapper 层。
- **避免 (DON'T)**: 将过多的业务逻辑写在 Controller 中。

## 重要提示

### Spring Boot 3 迁移注意事项

由于项目使用 Spring Boot 3.x，所有 `javax.*` 包已迁移到 `jakarta.*`：

- ✅ **正确**: `import jakarta.validation.constraints.NotNull;`
- ❌ **错误**: `import javax.validation.constraints.NotNull;`

**常用验证注解**:

- `jakarta.validation.constraints.NotNull`
- `jakarta.validation.constraints.NotBlank`
- `jakarta.validation.constraints.Size`
- `jakarta.validation.constraints.Email`

## 示例

**提示词:**

> 为“产品管理 (Product)”创建一个完整的 CRUD 后端模块。

**代码示例 (Controller):**

```java
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "产品管理")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "获取产品详情")
    public Result<ProductVO> getProduct(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }
}
```
