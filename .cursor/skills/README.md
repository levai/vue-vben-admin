# 技能目录（统一在根目录）

所有技能已集中在 **`.cursor/skills/`**，使用 **@.cursor/skills** 或 @ 具体技能目录即可触发。

**路径约定**：以仓库根为基准。前端指 **frontend/apps/web-antd**（应用内路径即 `src/` 下）；后端指 **backend**（Java 包在 `backend/src/main/java/com/vben/admin/`）。

## 前端技能

| 技能 | 何时用 |
|------|--------|
| frontend-development | 通用前端规范、组件优先级、API/权限要点 |
| crud-with-drawer | **完整 CRUD**（列表+搜索+抽屉/弹窗+删除+权限） |
| i18n | 国际化、多语言、locale |
| icons | 菜单/按钮图标、Iconify/Lucide |
| confirmations | 删除确认、message 反馈 |
| ui-component | 新建 Vue 3 组件 |
| permissions | 单独加权限、定义权限码 |
| api-integration | 单独对接新接口 |
| routes | 新路由、新菜单 |

## 后端技能

| 技能 | 何时用 |
|------|--------|
| backend-module | **完整后端 CRUD 模块**（Entity/DTO/VO/Mapper/Service/Controller、建表 SQL） |
| database-migration | 数据库迁移、SQL 脚本、Entity 同步 |

**说明**：当前 2 个技能覆盖大部分后端需求。认证/JWT、导出 Excel、定时任务等见 `.cursor/rules/backend-development.mdc` 对应章节；若某类需求高频可再增技能。

## 使用方式

- **触发全部**：@.cursor/skills
- **触发单个**：@.cursor/skills/crud-with-drawer、@.cursor/skills/backend-module 等

## 已移除（已合并）

- workflow-crud-page、workflow-modal-drawer → **crud-with-drawer**
- workflow-backend-module → **backend-module**
