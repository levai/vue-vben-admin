---
name: backend-module
description: 根据项目后端规范生成完整 CRUD 模块（Entity、DTO、VO、Mapper、Service、Controller、可选 OptionQueryDTO 与建表 SQL）。在用户要求做后端模块、某某管理接口、增删改查 API、Spring Boot CRUD 时使用。基于 backend 与 User/Role 参考实现。
---

# 后端模块生成技能

后端代码路径：**`backend/src/main/java/com/vben/admin/`**。规范详见 **项目根目录 `.cursor/rules/backend-development.mdc`** 与 **`.cursor/rules/命名规范说明.mdc`**。参考实现：**User**（`controller/UserController`、`service/UserService`、`model/entity/SysUser`、`model/dto/UserDTO`、`model/vo/UserVO`）、**Role**（`RoleController`、`RoleService` 等）。

## 何时使用本技能

- 用户要求：后端模块、XX 管理接口、增删改查 API、Spring Boot CRUD、新建实体/表
- 产出：Entity + DTO + VO + Mapper + Service 接口与实现 + Controller + 可选 OptionQueryDTO + 建表 SQL

## 命名与路径约定

- **业务名**：全模块统一（如 `User`、`Role`），仅后缀不同。
- **Entity**：`Sys` + 业务名，表名 `sys_xxx`（下划线）。
- **路径**：管理端 CRUD 统一 `@RequestMapping("/system/xxx")`，多词用短横线（如 `/system/operation-log`）。
- **包**：`controller`、`service`、`service.impl`、`mapper`、`model.entity`、`model.dto`、`model.vo`。

| 层级       | 命名               | 示例           |
|------------|--------------------|----------------|
| Controller | 业务名 + Controller | UserController |
| Service    | 业务名 + Service    | UserService    |
| ServiceImpl| 业务名 + ServiceImpl | UserServiceImpl |
| Mapper     | 业务名 + Mapper     | UserMapper     |
| Entity     | Sys + 业务名       | SysUser        |
| DTO        | 业务名 + DTO       | UserDTO        |
| VO         | 业务名 + VO        | UserVO         |

## 1. Entity（`model/entity/SysXxx.java`）

- `@TableName("sys_xxx")`，表名与库表一致。
- `@TableId(type = IdType.ASSIGN_ID)` 主键 String。
- 业务字段 + 审计字段：`createBy`、`createTime`、`updateBy`、`updateTime` 使用 `@TableField(fill = FieldFill.INSERT)` / `INSERT_UPDATE`。
- 逻辑删除：`@TableLogic` 的 `deleted` 字段（Integer，0 未删 1 已删）。
- 使用 Lombok `@Data`。

## 2. DTO（`model/dto/XxxDTO.java`）

- 用于创建/更新，不包含 id、createTime、updateTime。
- 字段加 `@Schema(description = "...")`，必填与格式用 Bean Validation：`@NotBlank`、`@NotNull`、`@Size`、`@Pattern` 等。
- **分组校验**：内部定义 `Create`、`Update` 接口（继承 Default），创建用 `Create`、更新用 `Update`，更新时可选字段不加 `groups` 或放在 `Update`。
- 列表/筛选条件若很多，可单独 **QueryDTO**（如 `XxxQueryDTO`）；选项接口用 **XxxOptionQueryDTO**，含 `limit`（默认 1000）及与列表一致的筛选字段。

## 3. VO（`model/vo/XxxVO.java`）

- 仅包含返回前端的字段；可含关联展示字段（如部门名、创建人姓名）。
- 所有字段 `@Schema(description = "...")`。
- 时间字段可用 `@JsonFormat` 或统一配置。

## 4. Mapper（`mapper/XxxMapper.java`）

- `public interface XxxMapper extends BaseMapper<SysXxx> {}`，无额外方法时保持空接口。
- 复杂 SQL 再在 XML 或注解中补充。

## 5. Service 接口（`service/XxxService.java`）

- 方法：`getXxxList(page, pageSize, ...查询参数)` → `PageResult<XxxVO>`；`getXxxById(String id)` → `XxxVO`；`createXxx(XxxDTO)` → `String`（返回 id）；`updateXxx(String id, XxxDTO)`；`deleteXxx(String id)`。
- 路径参数 id 在**接口方法参数**上加 `@ValidId(message = "xxxID不能为空或无效值")`。
- 若有选项接口：`getXxxOptions(XxxOptionQueryDTO queryDTO)` → `PageResult<XxxVO>`。

## 6. Service 实现（`service/impl/XxxServiceImpl.java`）

- `@Service`、`@RequiredArgsConstructor`，注入 Mapper 及所需依赖。
- **读**：分页用 `Page<SysXxx> pageParam = new Page<>(page, pageSize)`，`LambdaQueryWrapper<SysXxx>` 构建条件，`xxxMapper.selectPage(pageParam, queryWrapper)`，再 `stream().map(this::convertToVO).collect(toList())`，`return PageResult.of(voList, pageResult.getTotal())`。
- **条件**：用 `QueryHelper.applySearch(queryWrapper, SearchQueryConfig.of(search).searchField(...).fallbackField(...))` 处理 search；用 `QueryHelper.applyTimeRange(queryWrapper, startTime, endTime, SysXxx::getCreateTime)` 处理时间范围；其它字段 `StringUtils.hasText(xxx)` 时 `queryWrapper.eq(...)`；`orderByDesc(SysXxx::getCreateTime)`。
- **写**：创建/更新/删除方法加 `@Transactional(rollbackFor = Exception.class)`。先 `getXxxByIdOrThrow(id)`（内部 selectById，null 则 `throw new BusinessException("xxx不存在")`），再校验、再更新或删除。
- **私有方法**：`getXxxByIdOrThrow(String id)`、唯一性校验（如 `checkXxxNotExists`）、`buildXxxEntity(DTO, id)`、`updateXxxBasicInfo(entity, DTO)`、`convertToVO(SysXxx)`。禁止在 Controller 中写业务与直接操作 Mapper。

## 7. Controller（`controller/XxxController.java`）

- `@Tag(name = "系统xxx管理")`、`@RestController`、`@RequestMapping("/system/xxx")`、`@Validated`、`@RequiredArgsConstructor`，注入 `XxxService`。
- **列表**：`@GetMapping`，返回 `BaseResult<PageResult<XxxVO>>`，参数 `page`(默认 1)、`pageSize`(默认 20)、search、以及筛选字段、startTime、endTime，调 `xxxService.getXxxList(...)`，`return new BaseResult<>(result)`。
- **详情**：`@GetMapping("/{id}")`，`@ValidId @PathVariable String id`，返回 `BaseResult<XxxVO>`。
- **创建**：`@PostMapping`，`@Validated(XxxDTO.Create.class) @RequestBody XxxDTO dto`，返回 `BaseResult<String>`（id）。
- **更新**：`@PutMapping("/{id}")`，`@ValidId @PathVariable String id` + `@Validated(XxxDTO.Update.class) @RequestBody XxxDTO dto`，返回 `BaseResult<Boolean>`。
- **删除**：`@DeleteMapping("/{id}")`，`@ValidId @PathVariable String id`，返回 `BaseResult<Boolean>`。
- **选项**（如需）：`@GetMapping("/options")`，参数用 `XxxOptionQueryDTO`，返回 `BaseResult<PageResult<XxxVO>>`。
- 所有接口统一 `return new BaseResult<>(...)`；参数加 `@Parameter(description = "...")`。小型请求体可放在 Controller 内部静态类（如 `UpdateStatusDTO`）并加 `@Schema`。

## 8. 统一响应与工具

- 响应：`BaseResult<T>`（code、message、data）；分页：`PageResult<T>`（list、total）。
- 业务异常：`throw new BusinessException("中文消息")`。
- ID 校验：Controller/Service 用 `@ValidId`；工具 `ValidationUtils.isValidId/isInvalidId`、`QueryHelper.applyTimeRange`、`QueryHelper.applySearch`、`QueryHelper.applyLimit`（options 用）。

## 9. 建表 SQL（可选）

- 表名 `sys_xxx`，主键 `id` varchar(64)，`create_by`、`create_time`、`update_by`、`update_time`、`deleted` 与项目现有表一致；需要时加索引（如唯一、查询条件）。

## 10. 生成顺序与检查清单

**推荐生成顺序**：Entity → DTO/VO → Mapper → Service 接口 → Service 实现 → Controller → OptionQueryDTO（如需）→ 建表 SQL（如需）。

- [ ] Entity：表名、主键、审计字段、逻辑删除
- [ ] DTO：分组校验 Create/Update、@Schema
- [ ] VO：仅返回字段、@Schema
- [ ] Mapper：extends BaseMapper<SysXxx>
- [ ] Service：id 参数带 @ValidId；写操作 @Transactional
- [ ] Controller：@Tag、@Validated、BaseResult、路径 /system/xxx、REST 方法正确
- [ ] 列表参数含 search、startTime、endTime 时与 QueryHelper 用法一致
- [ ] 异常与校验使用 BusinessException、ValidationUtils

## 11. 参考文件速查

以下路径均以 **backend/** 为前缀，即 `backend/src/main/java/com/vben/admin/` 下：

| 用途     | 路径（backend/ 下） |
|----------|----------------------|
| Controller | src/main/java/com/vben/admin/controller/UserController.java、RoleController.java |
| Service 接口 | service/UserService.java |
| Service 实现 | service/impl/UserServiceImpl.java（参考 getXxxByIdOrThrow、QueryHelper、分页） |
| Entity   | model/entity/SysUser.java |
| DTO      | model/dto/UserDTO.java、RoleDTO.java |
| VO       | model/vo/UserVO.java |
| Mapper   | mapper/UserMapper.java |
| 选项查询 DTO | model/dto/UserOptionQueryDTO.java |
| 规范     | 根目录 .cursor/rules/backend-development.mdc、命名规范说明.mdc |

更细的 Service 优化模式、参数校验、异常处理见根目录 `.cursor/rules/backend-development.mdc`。认证/JWT、导出等非 CRUD 需求也见该规范对应章节；当前无单独技能，必要时可增。
