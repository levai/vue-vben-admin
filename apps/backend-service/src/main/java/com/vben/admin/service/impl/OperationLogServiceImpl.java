package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vben.admin.core.enums.OperationType;
import com.vben.admin.core.utils.QueryHelper;
import com.vben.admin.core.utils.SearchQueryConfig;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.mapper.OperationLogMapper;
import com.vben.admin.model.dto.OperationLogQueryDTO;
import com.vben.admin.model.entity.SysOperationLog;
import com.vben.admin.model.vo.MenuVO;
import com.vben.admin.model.vo.OperationLogVO;
import com.vben.admin.model.vo.TreeOptionVO;
import com.vben.admin.service.MenuService;
import com.vben.admin.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现
 *
 * @author vben
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    /**
     * 菜单类型：按钮
     */
    private static final String MENU_TYPE_BUTTON = "button";

    /**
     * 个人中心模块值
     */
    private static final String MODULE_PROFILE = "profile";

    /**
     * 个人中心模块标签
     */
    private static final String MODULE_PROFILE_LABEL = "个人中心";

    private final OperationLogMapper operationLogMapper;
    private final MenuService menuService;
    private final com.vben.admin.core.utils.MenuModuleResolver menuModuleResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOperationLog(SysOperationLog operationLog) {
        operationLogMapper.insert(operationLog);
    }

    @Override
    @Async("operationLogExecutor")
    public void saveOperationLogAsync(SysOperationLog operationLog) {
        try {
            saveOperationLog(operationLog);
        } catch (Exception e) {
            log.error("异步保存操作日志失败", e);
        }
    }

    @Override
    public PageResult<OperationLogVO> getOperationLogList(OperationLogQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<SysOperationLog> queryWrapper = buildQueryWrapper(queryDTO);

        // 分页查询
        Page<SysOperationLog> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<SysOperationLog> pageResult = operationLogMapper.selectPage(pageParam, queryWrapper);

        // 转换为VO
        List<OperationLogVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal());
    }

    @Override
    public OperationLogVO getOperationLogDetail(String id) {
        SysOperationLog operationLog = operationLogMapper.selectById(id);
        return operationLog != null ? convertToVO(operationLog) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOperationLog(String id) {
        operationLogMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteOperationLog(List<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            operationLogMapper.deleteBatchIds(ids);
        }
    }

    /**
     * 构建查询条件
     *
     * @param queryDTO 查询DTO
     * @return 查询包装器
     */
    private LambdaQueryWrapper<SysOperationLog> buildQueryWrapper(OperationLogQueryDTO queryDTO) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();

        // 用户ID精确查询
        if (StringUtils.hasText(queryDTO.getUserId())) {
            queryWrapper.eq(SysOperationLog::getUserId, queryDTO.getUserId());
        }

        // 用户名模糊查询
        if (StringUtils.hasText(queryDTO.getUsername())) {
            queryWrapper.like(SysOperationLog::getUsername, queryDTO.getUsername());
        }

        // 操作类型精确查询
        if (StringUtils.hasText(queryDTO.getOperationType())) {
            queryWrapper.eq(SysOperationLog::getOperationType, queryDTO.getOperationType());
        }

        // 操作模块查询（需要转换为中文名称）
        if (StringUtils.hasText(queryDTO.getOperationModule())) {
            applyOperationModuleQuery(queryWrapper, queryDTO.getOperationModule());
        }

        // 状态查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(SysOperationLog::getStatus, queryDTO.getStatus());
        }

        // 时间范围查询
        QueryHelper.applyTimeRange(queryWrapper, queryDTO.getStartTime(), queryDTO.getEndTime(), SysOperationLog::getCreateTime);

        // 搜索关键词（请求URL）
        QueryHelper.applySearch(
                queryWrapper,
                SearchQueryConfig.<SysOperationLog>of(queryDTO.getSearch())
                        .searchField(SysOperationLog::getRequestUrl)
        );

        // 按创建时间倒序
        queryWrapper.orderByDesc(SysOperationLog::getCreateTime);

        return queryWrapper;
    }

    /**
     * 应用操作模块查询条件
     *
     * @param queryWrapper 查询包装器
     * @param moduleValue  模块值（可能是英文值或中文名称）
     */
    private void applyOperationModuleQuery(LambdaQueryWrapper<SysOperationLog> queryWrapper, String moduleValue) {
        // 将英文值（如 "menu"）转换为中文菜单名称（如 "菜单管理"）进行查询
        // 因为数据库中存储的是中文菜单名称
        String moduleLabel = convertModuleValueToLabel(moduleValue);
        if (moduleLabel != null) {
            queryWrapper.eq(SysOperationLog::getOperationModule, moduleLabel);
        } else {
            // 如果转换失败，尝试直接使用原值查询（可能是中文名称直接传入）
            queryWrapper.eq(SysOperationLog::getOperationModule, moduleValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredLogs(LocalDateTime expireTime) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(SysOperationLog::getCreateTime, expireTime);
        int count = operationLogMapper.selectCount(queryWrapper).intValue();
        operationLogMapper.delete(queryWrapper);
        return count;
    }

    @Override
    public PageResult<TreeOptionVO> getOperationModuleList(String search) {
        // 获取菜单树形结构
        List<MenuVO> menuTree = menuService.getMenuList(null);

        // 转换为 TreeOptionVO 树形结构
        List<TreeOptionVO> optionTree = convertMenuTreeToOptionTree(menuTree, search);

        // 添加固定的"个人中心"选项（不在菜单树中）
        TreeOptionVO profileOption = createProfileOption();
        if (shouldIncludeOption(profileOption, search)) {
            optionTree.add(profileOption);
        }

        return new PageResult<>(optionTree, (long) countOptions(optionTree));
    }

    /**
     * 将菜单树转换为选项树
     *
     * @param menuTree 菜单树
     * @param search   搜索关键词（可选）
     * @return 选项树
     */
    private List<TreeOptionVO> convertMenuTreeToOptionTree(List<MenuVO> menuTree, String search) {
        if (menuTree == null || menuTree.isEmpty()) {
            return new ArrayList<>();
        }

        List<TreeOptionVO> result = new ArrayList<>();
        for (MenuVO menu : menuTree) {
            // 排除 button 类型
            if (MENU_TYPE_BUTTON.equals(menu.getType())) {
                continue;
            }

            // value: 从 path 提取模块名（如 /system/user -> user）
            // catalog 类型可能没有 path，value 为 null（作为父级，不可选择）
            String value = extractModuleFromPath(menu.getPath());

            // 递归处理子菜单
            List<TreeOptionVO> children = new ArrayList<>();
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                children = convertMenuTreeToOptionTree(menu.getChildren(), search);
            }

            // 如果当前菜单有 value（有 path），则创建选项
            if (value != null && !value.isEmpty()) {
                TreeOptionVO option = new TreeOptionVO();
                // label: 从 meta.title 获取，如果没有则使用 name
                String title = extractMenuTitle(menu);
                option.setLabel(title);
                option.setValue(value);

                // 设置子菜单（保持树形结构）
                if (!children.isEmpty()) {
                    option.setChildren(children);
                }

                // 判断是否应该包含此选项
                if (shouldIncludeOption(option, search, children)) {
                    result.add(option);
                }
            } else {
                // 如果当前菜单没有 value（可能是 catalog 类型），但有子菜单，则直接添加子菜单
                if (!children.isEmpty()) {
                    result.addAll(children);
                }
            }
        }

        return result;
    }

    /**
     * 判断是否应该包含选项
     *
     * @param option  选项
     * @param search  搜索关键词
     * @param children 子选项列表（可选）
     * @return 是否应该包含
     */
    private boolean shouldIncludeOption(TreeOptionVO option, String search, List<TreeOptionVO> children) {
        // 如果没有搜索条件，包含所有选项
        if (!StringUtils.hasText(search)) {
            return true;
        }
        // 如果当前选项匹配搜索条件，包含
        if (matchesSearch(option, search)) {
            return true;
        }
        // 如果有子选项匹配搜索条件，包含（即使当前选项不匹配）
        if (children != null && !children.isEmpty() && hasMatchingChildren(children, search)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否应该包含选项（无子选项版本）
     *
     * @param option 选项
     * @param search 搜索关键词
     * @return 是否应该包含
     */
    private boolean shouldIncludeOption(TreeOptionVO option, String search) {
        return shouldIncludeOption(option, search, null);
    }

    /**
     * 检查子选项中是否有匹配搜索条件的
     *
     * @param children 子选项列表
     * @param search   搜索关键词
     * @return 是否有匹配的子选项
     */
    private boolean hasMatchingChildren(List<TreeOptionVO> children, String search) {
        if (children == null || children.isEmpty() || !StringUtils.hasText(search)) {
            return false;
        }
        for (TreeOptionVO child : children) {
            if (matchesSearch(child, search) || hasMatchingChildren(child.getChildren(), search)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查选项是否匹配搜索条件
     *
     * @param option 选项
     * @param search 搜索关键词
     * @return 是否匹配
     */
    private boolean matchesSearch(TreeOptionVO option, String search) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String lowerSearch = search.toLowerCase();
        return option.getLabel().contains(search) ||
               (option.getValue() != null && option.getValue().toLowerCase().contains(lowerSearch));
    }

    /**
     * 创建个人中心选项
     *
     * @return 个人中心选项
     */
    private TreeOptionVO createProfileOption() {
        TreeOptionVO profileOption = new TreeOptionVO();
        profileOption.setLabel(MODULE_PROFILE_LABEL);
        profileOption.setValue(MODULE_PROFILE);
        return profileOption;
    }

    /**
     * 从菜单中提取标题
     */
    private String extractMenuTitle(MenuVO menu) {
        // 优先从 meta.title 获取
        if (menu.getMeta() != null && menu.getMeta().get("title") != null) {
            return menu.getMeta().get("title").toString();
        }
        // 如果没有 title，使用 name
        return menu.getName();
    }

    /**
     * 从路径中提取模块名，并转换为与存储格式一致的格式
     * 例如：/system/user -> user
     *      /system/operation-log -> operation-log
     * 注意：返回的格式需要与 MenuModuleResolver.normalizeModulePath 转换后的格式一致
     */
    private String extractModuleFromPath(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        // 移除开头的斜杠
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        // 如果路径包含多个部分，取最后一部分
        String[] parts = cleanPath.split("/");
        String moduleName = parts.length > 0 ? parts[parts.length - 1] : cleanPath;

        if (moduleName == null || moduleName.isEmpty()) {
            return null;
        }

        // 转换为小写，下划线转横线（与 MenuModuleResolver.normalizeModulePath 的默认处理一致）
        return moduleName.toLowerCase().replace("_", "-");
    }

    /**
     * 将英文模块值（如 "menu"）转换为中文菜单名称（如 "菜单管理"）
     * 用于查询时匹配数据库中存储的中文名称
     *
     * @param moduleValue 英文模块值（如 "menu", "user", "operation-log"）
     * @return 中文菜单名称，如果找不到则返回 null
     */
    private String convertModuleValueToLabel(String moduleValue) {
        if (!StringUtils.hasText(moduleValue)) {
            return null;
        }

        // 特殊处理 "profile"
        if (MODULE_PROFILE.equals(moduleValue)) {
            return MODULE_PROFILE_LABEL;
        }

        // 获取菜单树
        List<MenuVO> menuTree = menuService.getMenuList(null);

        // 递归查找匹配的菜单
        MenuVO matchedMenu = findMenuByModuleValue(menuTree, moduleValue);
        if (matchedMenu != null) {
            return extractMenuTitle(matchedMenu);
        }

        // 如果找不到，尝试使用 MenuModuleResolver
        try {
            String label = menuModuleResolver.getModuleLabel(moduleValue);
            if (label != null && !label.equals(moduleValue)) {
                return label;
            }
        } catch (Exception e) {
            log.debug("使用 MenuModuleResolver 转换失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 在菜单树中查找匹配的菜单（根据 path 提取的模块值）
     *
     * @param menuTree   菜单树
     * @param moduleValue 模块值（如 "menu", "user"）
     * @return 匹配的菜单，如果找不到则返回 null
     */
    private MenuVO findMenuByModuleValue(List<MenuVO> menuTree, String moduleValue) {
        if (menuTree == null || menuTree.isEmpty()) {
            return null;
        }

        for (MenuVO menu : menuTree) {
            // 排除 button 类型
            if (MENU_TYPE_BUTTON.equals(menu.getType())) {
                continue;
            }

            // 提取当前菜单的模块值
            String menuModuleValue = extractModuleFromPath(menu.getPath());
            if (moduleValue.equals(menuModuleValue)) {
                return menu;
            }

            // 递归查找子菜单
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                MenuVO childMenu = findMenuByModuleValue(menu.getChildren(), moduleValue);
                if (childMenu != null) {
                    return childMenu;
                }
            }
        }

        return null;
    }

    /**
     * 统计选项数量（包括子节点）
     */
    private int countOptions(List<TreeOptionVO> options) {
        if (options == null || options.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (TreeOptionVO option : options) {
            count++;
            if (option.getChildren() != null && !option.getChildren().isEmpty()) {
                count += countOptions(option.getChildren());
            }
        }
        return count;
    }

    @Override
    public PageResult<TreeOptionVO> getOperationTypeList(String search) {
        // 从枚举中获取所有操作类型，转换为选项格式（label: 中文, value: 英文小写）
        List<TreeOptionVO> allOptions = Arrays.stream(OperationType.values())
                .map(this::convertOperationTypeToOption)
                .sorted((a, b) -> a.getLabel().compareTo(b.getLabel()))
                .collect(Collectors.toList());

        // 搜索关键词过滤
        if (StringUtils.hasText(search)) {
            allOptions = filterOptionsBySearch(allOptions, search);
        }

        return new PageResult<>(allOptions, (long) allOptions.size());
    }

    /**
     * 将操作类型转换为选项
     *
     * @param type 操作类型
     * @return 选项
     */
    private TreeOptionVO convertOperationTypeToOption(OperationType type) {
        TreeOptionVO option = new TreeOptionVO();
        option.setLabel(type.getLabel());
        option.setValue(type.name().toLowerCase());
        return option;
    }

    /**
     * 根据搜索关键词过滤选项
     *
     * @param options 选项列表
     * @param search  搜索关键词
     * @return 过滤后的选项列表
     */
    private List<TreeOptionVO> filterOptionsBySearch(List<TreeOptionVO> options, String search) {
        String lowerSearch = search.toLowerCase();
        return options.stream()
                .filter(option -> option.getLabel().contains(search)
                        || (option.getValue() != null && option.getValue().toLowerCase().contains(lowerSearch)))
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private OperationLogVO convertToVO(SysOperationLog operationLog) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(operationLog, vo);

        // 操作模块已经存储为菜单名称，直接使用，无需转换
        // 如果 operationModule 为空，尝试从 operationPage 重新获取菜单名称
        if ((vo.getOperationModule() == null || vo.getOperationModule().isEmpty())
                && vo.getOperationPage() != null && !vo.getOperationPage().isEmpty()) {
            String moduleName = menuModuleResolver.extractModuleFromPath(vo.getOperationPage());
            if (moduleName != null && !moduleName.isEmpty()) {
                vo.setOperationModule(moduleName);
            }
        }

        return vo;
    }
}
