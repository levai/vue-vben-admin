package com.vben.admin.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树形结构构建工具类
 * 提供通用的树形结构构建方法，减少代码重复
 *
 * @author vben
 */
@Slf4j
public class TreeHelper {

    /**
     * 根节点ID常量
     */
    public static final String ROOT_ID = "0";

    /**
     * 构建树形结构（通用方法）
     *
     * @param items       数据列表
     * @param getId       获取ID的方法
     * @param getPid      获取父ID的方法
     * @param getChildren 获取子节点列表的方法
     * @param setChildren 设置子节点列表的方法（parent, childrenList）
     * @param rootId      根节点ID（默认为 "0"）
     * @param <T>         数据类型
     * @return 树形结构列表
     * @throws IllegalArgumentException 如果方法引用为空
     */
    public static <T> List<T> buildTree(
            List<T> items,
            Function<T, String> getId,
            Function<T, String> getPid,
            Function<T, List<T>> getChildren,
            BiConsumer<T, List<T>> setChildren,
            String rootId
    ) {
        // 参数校验
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        if (getId == null || getPid == null || getChildren == null || setChildren == null) {
            throw new IllegalArgumentException("方法引用不能为空");
        }

        String actualRootId = rootId != null ? rootId : ROOT_ID;

        // 转换为 Map，便于快速查找（如果存在重复ID，保留第一个并记录警告）
        Map<String, T> itemMap = items.stream()
                .collect(Collectors.toMap(
                        getId,
                        item -> item,
                        (existing, replacement) -> {
                            log.warn("发现重复的ID: {}, 保留第一个元素", getId.apply(existing));
                            return existing; // 如果存在重复ID，保留第一个
                        }
                ));

        // 构建树形结构
        List<T> rootItems = new ArrayList<>();
        int orphanCount = 0; // 统计孤儿节点（父节点不存在）

        for (T item : items) {
            String itemId = getId.apply(item);
            String pid = getPid.apply(item);

            // 根节点处理
            if (actualRootId.equals(pid) || pid == null || pid.isEmpty()) {
                rootItems.add(item);
            } else {
                // 查找父节点
                T parent = itemMap.get(pid);
                if (parent != null) {
                    List<T> children = getChildren.apply(parent);
                    if (children == null) {
                        children = new ArrayList<>();
                        setChildren.accept(parent, children);
                    }
                    children.add(item);
                } else {
                    // 父节点不存在，记录警告
                    orphanCount++;
                    log.warn("发现孤儿节点: ID={}, 父ID={} 不存在", itemId, pid);
                    // 将孤儿节点也添加到根节点（避免数据丢失）
                    rootItems.add(item);
                }
            }
        }

        if (orphanCount > 0) {
            log.warn("构建树形结构时发现 {} 个孤儿节点，已将其添加到根节点", orphanCount);
        }

        return rootItems;
    }

    /**
     * 构建树形结构（使用默认根节点ID "0"）
     *
     * @param items       数据列表
     * @param getId       获取ID的方法
     * @param getPid      获取父ID的方法
     * @param getChildren 获取子节点列表的方法
     * @param setChildren 设置子节点列表的方法
     * @param <T>         数据类型
     * @return 树形结构列表
     */
    public static <T> List<T> buildTree(
            List<T> items,
            Function<T, String> getId,
            Function<T, String> getPid,
            Function<T, List<T>> getChildren,
            BiConsumer<T, List<T>> setChildren
    ) {
        return buildTree(items, getId, getPid, getChildren, setChildren, ROOT_ID);
    }
}
