package com.vben.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vben.admin.core.enums.OperationType;
import com.vben.admin.core.utils.OperationInfoParser;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.model.vo.MenuVO;
import com.vben.admin.model.vo.TreeOptionVO;

import java.util.Arrays;
import java.util.ArrayList;
import com.vben.admin.mapper.OperationLogMapper;
import com.vben.admin.model.dto.OperationLogQueryDTO;
import com.vben.admin.model.entity.SysOperationLog;
import com.vben.admin.model.vo.OperationLogVO;
import com.vben.admin.service.MenuService;
import com.vben.admin.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
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
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getUserId())) {
            queryWrapper.eq(SysOperationLog::getUserId, queryDTO.getUserId());
        }
        if (StringUtils.hasText(queryDTO.getUsername())) {
            queryWrapper.like(SysOperationLog::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getOperationType())) {
            // 直接使用英文值查询
            queryWrapper.eq(SysOperationLog::getOperationType, queryDTO.getOperationType());
        }
        if (StringUtils.hasText(queryDTO.getOperationModule())) {
            // 直接使用英文值查询（数据库中存储的是横线格式，如 "operation-log"）
            queryWrapper.eq(SysOperationLog::getOperationModule, queryDTO.getOperationModule());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(SysOperationLog::getStatus, queryDTO.getStatus());
        }

        // 时间范围查询
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            try {
                LocalDate startDate = LocalDate.parse(queryDTO.getStartTime(), DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime startDateTime = startDate.atStartOfDay();
                queryWrapper.ge(SysOperationLog::getCreateTime, startDateTime);
            } catch (DateTimeParseException e) {
                log.warn("无效的开始时间格式: {}", queryDTO.getStartTime());
            }
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            try {
                LocalDate endDate = LocalDate.parse(queryDTO.getEndTime(), DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                queryWrapper.le(SysOperationLog::getCreateTime, endDateTime);
            } catch (DateTimeParseException e) {
                log.warn("无效的结束时间格式: {}", queryDTO.getEndTime());
            }
        }

        // 搜索关键词（请求URL）
        if (StringUtils.hasText(queryDTO.getSearch())) {
            queryWrapper.like(SysOperationLog::getRequestUrl, queryDTO.getSearch());
        }

        queryWrapper.orderByDesc(SysOperationLog::getCreateTime);

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
        if (operationLog == null) {
            return null;
        }
        return convertToVO(operationLog);
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
        List<MenuVO> menuTree = menuService.getMenuList();

        // 转换为 TreeOptionVO 树形结构
        List<TreeOptionVO> optionTree = convertMenuTreeToOptionTree(menuTree, search);

        // 添加固定的"个人中心"选项（不在菜单树中）
        TreeOptionVO profileOption = new TreeOptionVO();
        profileOption.setLabel("个人中心");
        profileOption.setValue("profile");

        // 如果搜索关键词匹配"个人中心"或"profile"，则添加
        if (!StringUtils.hasText(search) ||
            matchesSearch(profileOption, search)) {
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
            if ("button".equals(menu.getType())) {
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

                // 搜索过滤：如果匹配搜索条件，则保留
                if (matchesSearch(option, search)) {
                    result.add(option);
                }
            }

            // 如果有子菜单且匹配搜索条件，则添加子菜单选项
            if (!children.isEmpty()) {
                result.addAll(children);
            }
        }

        return result;
    }

    /**
     * 检查选项是否匹配搜索条件
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
                .map(type -> {
                    TreeOptionVO option = new TreeOptionVO();
                    option.setLabel(type.getLabel());
                    option.setValue(type.name().toLowerCase());
                    return option;
                })
                .sorted((a, b) -> a.getLabel().compareTo(b.getLabel()))
                .collect(Collectors.toList());

        // 搜索关键词过滤
        if (StringUtils.hasText(search)) {
            String lowerSearch = search.toLowerCase();
            allOptions = allOptions.stream()
                    .filter(option -> option.getLabel().contains(search)
                            || option.getValue().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toList());
        }

        return new PageResult<>(allOptions, (long) allOptions.size());
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
