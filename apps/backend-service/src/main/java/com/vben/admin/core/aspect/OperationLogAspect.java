package com.vben.admin.core.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vben.admin.core.model.BaseResult;
import com.vben.admin.core.model.PageResult;
import com.vben.admin.core.utils.BrowserInfoParser;
import com.vben.admin.core.utils.OperationInfoParser;
import com.vben.admin.core.utils.SensitiveDataFilter;
import com.vben.admin.core.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.admin.model.entity.SysOperationLog;
import com.vben.admin.model.entity.SysUser;
import com.vben.admin.mapper.UserMapper;
import com.vben.admin.service.MenuService;
import com.vben.admin.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作日志AOP切面
 *
 * @author vben
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final UserMapper userMapper;
    private final MenuService menuService;
    private final com.vben.admin.core.utils.MenuModuleResolver menuModuleResolver;

    // 配置 ObjectMapper，确保能正确序列化所有对象
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // 注册 Java 8 时间类型支持
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    /**
     * 需要排除的URL路径（登录和登出需要记录，所以不排除）
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/actuator",
            "/doc.html",
            "/swagger",
            "/v3/api-docs",
            "/favicon.ico"
    );

    /**
     * 拦截所有RestController的方法
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        String requestUrl = request.getRequestURI();

        // 排除不需要记录的接口
        if (shouldExclude(requestUrl)) {
            return joinPoint.proceed();
        }

        // 获取请求信息
        String requestMethod = request.getMethod();
        String requestParams = getRequestParams(joinPoint, request);

        // 判断是否为登录或登出操作
        boolean isLogin = requestUrl.contains("/auth/login");
        boolean isLogout = requestUrl.contains("/auth/logout");

        // 获取用户信息
        String userId = null;
        String username = null;
        String realName = null;

        if (isLogin) {
            // 登录操作：从请求参数中获取用户名（此时用户还未认证）
            username = extractUsernameFromLoginRequest(joinPoint);
            if (StringUtils.hasText(username)) {
            // 根据用户名查询用户信息
            SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username)
                    .last("LIMIT 1")
            );
                if (user != null) {
                    userId = user.getId();
                    realName = user.getRealName();
                }
            }
        } else if (isLogout) {
            // 登出操作：在执行前获取用户信息（执行后用户可能已退出）
            // 优先从 request 属性中获取（JwtAuthenticationFilter 设置的）
            Object requestUserId = request.getAttribute("userId");
            Object requestUsername = request.getAttribute("username");
            if (requestUserId != null) {
                userId = requestUserId.toString();
            } else {
                userId = SecurityUtils.getCurrentUserId();
            }
            if (requestUsername != null) {
                username = requestUsername.toString();
            } else {
                username = SecurityUtils.getCurrentUsername();
            }
            realName = getRealName(userId);
        } else {
            // 其他操作：正常获取用户信息
            userId = SecurityUtils.getCurrentUserId();
            username = SecurityUtils.getCurrentUsername();
            realName = getRealName(userId);
        }

        // 获取IP和浏览器信息
        String ipAddress = getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        BrowserInfoParser.BrowserInfo browserInfo = BrowserInfoParser.parseUserAgent(userAgent);

        // 获取前端页面URL（优先从Referer获取，后端自动解析，无需前端发送Header）
        // 如果Referer不可用，再尝试从自定义Header获取（作为降级方案）
        String pageUrl = null;

        // 1. 优先从Referer获取（浏览器自动发送，更可靠）
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            try {
                java.net.URL url = new java.net.URL(referer);
                pageUrl = url.getPath();
                // 去掉查询参数和锚点
                if (pageUrl.contains("?")) {
                    pageUrl = pageUrl.substring(0, pageUrl.indexOf("?"));
                }
                if (pageUrl.contains("#")) {
                    pageUrl = pageUrl.substring(0, pageUrl.indexOf("#"));
                }
                // 确保路径以 / 开头
                if (pageUrl != null && !pageUrl.isEmpty() && !pageUrl.startsWith("/")) {
                    pageUrl = "/" + pageUrl;
                }
            } catch (Exception e) {
                log.debug("解析Referer失败: {}", e.getMessage());
            }
        }

        // 2. 如果Referer不可用，降级使用自定义Header（前端可能已发送）
        if (pageUrl == null || pageUrl.isEmpty()) {
            pageUrl = request.getHeader("X-Page-Url");
        }

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 执行方法
        Object result = null;
        int status = 1; // 成功
        String errorMessage = null;
        String responseData = null;
        Integer responseCode = 200;

        try {
            result = joinPoint.proceed();
            responseData = getResponseData(result);

            // 获取 HTTP 响应状态码
            if (response != null) {
                responseCode = response.getStatus();
            }

            // 如果返回的是 BaseResult，也记录业务状态码
            if (result instanceof BaseResult) {
                BaseResult<?> baseResult = (BaseResult<?>) result;
                int businessCode = baseResult.getCode();
                if (businessCode != 0) {
                    status = 0; // 失败
                    errorMessage = baseResult.getMessage();
                    // 如果 HTTP 状态码是 200，但业务状态码不是 0，说明业务失败
                    if (responseCode == 200) {
                        // 根据业务状态码设置 HTTP 状态码（可选，这里保持原样）
                        // responseCode = businessCode >= 400 ? businessCode : responseCode;
                    }
                }
            }
        } catch (Exception e) {
            status = 0; // 失败
            errorMessage = e.getMessage();
            // 异常时获取响应状态码
            if (response != null) {
                responseCode = response.getStatus();
                // 如果状态码还是默认值，说明可能是未捕获的异常
                if (responseCode == 200) {
                    responseCode = 500; // 服务器内部错误
                }
            } else {
                responseCode = 500;
            }
            throw e;
        } finally {
            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;

            // 解析操作信息（从URL和方法推断，使用菜单模块解析器）
            OperationInfoParser.OperationInfo operationInfo = OperationInfoParser.parseOperationInfo(
                    requestUrl, requestMethod, pageUrl, menuModuleResolver);

            // 根据页面路径查询菜单名称链（格式：父菜单 - 子菜单）
            String menuNameChain = null;
            String pageName = null;
            if (pageUrl != null && !pageUrl.isEmpty()) {
                try {
                    menuNameChain = menuService.getMenuNameChainByPath(pageUrl);
                    if (menuNameChain != null) {
                        // 从名称链中提取页面名称（最后一个）
                        String[] parts = menuNameChain.split(" - ");
                        if (parts.length > 0) {
                            pageName = parts[parts.length - 1];
                        }
                    }
                } catch (Exception e) {
                    log.debug("查询菜单名称链失败: {}", e.getMessage());
                }
            }


            // 构建操作日志实体
            SysOperationLog operationLog = new SysOperationLog();
            // 对于登录和登出操作，如果用户信息为空，设置默认值
            if (isLogin || isLogout) {
                if (userId == null || username == null) {
                    // 如果登出时无法获取用户信息，尝试从请求属性中获取
                    if (isLogout) {
                        Object requestUserId = request.getAttribute("userId");
                        Object requestUsername = request.getAttribute("username");
                        if (requestUserId != null) {
                            userId = requestUserId.toString();
                        }
                        if (requestUsername != null) {
                            username = requestUsername.toString();
                        }
                    }
                    // 如果仍然为空，设置默认值（避免数据库约束错误）
                    if (userId == null) {
                        userId = "unknown";
                    }
                    if (username == null) {
                        username = "unknown";
                    }
                }
            }
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setRealName(realName);
            operationLog.setOperationType(operationInfo.getOperationType());
            // 操作模块存储英文值（operationInfo 已经返回英文值）
            operationLog.setOperationModule(operationInfo.getOperationModule());
            operationLog.setOperationPage(operationInfo.getOperationPage());
            operationLog.setPageName(pageName);
            operationLog.setRequestMethod(requestMethod);
            operationLog.setRequestUrl(requestUrl);
            operationLog.setRequestParams(SensitiveDataFilter.filterSensitiveData(requestParams));
            operationLog.setResponseCode(responseCode);
            operationLog.setResponseData(SensitiveDataFilter.filterSensitiveData(responseData));
            operationLog.setIpAddress(ipAddress);
            operationLog.setUserAgent(userAgent);
            operationLog.setBrowser(browserInfo.getBrowser());
            operationLog.setOs(browserInfo.getOs());
            operationLog.setDuration((int) duration);
            operationLog.setStatus(status);
            operationLog.setErrorMessage(errorMessage);

            // 异步保存日志
            operationLogService.saveOperationLogAsync(operationLog);
        }

        return result;
    }


    /**
     * 判断是否应该排除
     */
    private boolean shouldExclude(String requestUrl) {
        if (requestUrl == null) {
            return true;
        }
        return EXCLUDE_PATHS.stream().anyMatch(requestUrl::startsWith);
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        try {
            String requestMethod = request.getMethod();
            Map<String, Object> paramsMap = new HashMap<>();

            // GET 请求：只记录查询参数，不记录 DTO 对象（避免重复）
            if ("GET".equals(requestMethod)) {
                String queryString = request.getQueryString();
                if (queryString != null && !queryString.isEmpty()) {
                    // 解析查询参数
                    String[] pairs = queryString.split("&");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split("=", 2);
                        if (keyValue.length == 2) {
                            try {
                                paramsMap.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], "UTF-8"));
                            } catch (Exception e) {
                                paramsMap.put(keyValue[0], keyValue[1]);
                            }
                        } else if (keyValue.length == 1) {
                            // 只有 key，没有 value
                            paramsMap.put(keyValue[0], "");
                        }
                    }
                }
            } else {
                // POST/PUT/DELETE 请求：记录 RequestBody 或 PathVariable
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    // 过滤掉HttpServletRequest和HttpServletResponse等框架对象
                    List<Object> filteredArgs = Arrays.stream(args)
                            .filter(arg -> !(arg instanceof HttpServletRequest))
                            .filter(arg -> !(arg instanceof HttpServletResponse))
                            .filter(arg -> arg != null)
                            .collect(Collectors.toList());

                    if (!filteredArgs.isEmpty()) {
                        if (filteredArgs.size() == 1) {
                            // 如果只有一个参数
                            Object arg = filteredArgs.get(0);
                            if (arg instanceof Map) {
                                // Map 类型，直接合并
                                paramsMap.putAll((Map<String, Object>) arg);
                            } else if (isSimpleType(arg)) {
                                // 简单类型（String、Number、Boolean等），直接存储值
                                paramsMap.put("body", arg);
                            } else {
                                // 复杂对象，序列化为 JSON
                                paramsMap.putAll(objectMapper.convertValue(arg, Map.class));
                            }
                        } else {
                            // 多个参数，序列化为 JSON
                            String jsonStr = objectMapper.writeValueAsString(filteredArgs);
                            paramsMap.put("body", jsonStr);
                        }
                    }
                }

                // POST/PUT/DELETE 请求也记录查询参数（如果有）
                String queryString = request.getQueryString();
                if (queryString != null && !queryString.isEmpty()) {
                    String[] pairs = queryString.split("&");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split("=", 2);
                        if (keyValue.length == 2) {
                            try {
                                paramsMap.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], "UTF-8"));
                            } catch (Exception e) {
                                paramsMap.put(keyValue[0], keyValue[1]);
                            }
                        }
                    }
                }
            }

            // 返回合并后的参数（统一序列化为 JSON 字符串）
            if (paramsMap.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(paramsMap);
        } catch (Exception e) {
            log.debug("获取请求参数失败: {}", e.getMessage());
            // 降级处理：只返回查询字符串
            String queryString = request.getQueryString();
            return queryString != null && !queryString.isEmpty() ? queryString : null;
        }
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> clazz = obj.getClass();
        // 基本类型及其包装类
        return clazz.isPrimitive()
                || obj instanceof String
                || obj instanceof Number
                || obj instanceof Boolean
                || obj instanceof Character;
    }

    /**
     * 获取响应数据
     * 统一序列化为 JSON 字符串格式
     * 保持完整的 BaseResult 格式（包含 code、message、data）
     * 确保始终返回 JSON 字符串，不使用 toString()
     */
    private String getResponseData(Object result) {
        if (result == null) {
            return null;
        }
        try {
            // 统一序列化为 JSON 字符串（保持 BaseResult 的完整格式）
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.warn("序列化响应数据失败，尝试降级处理: {}", e.getMessage());
            // 降级处理：如果序列化失败，尝试手动构建 JSON
            try {
                if (result instanceof BaseResult) {
                    BaseResult<?> baseResult = (BaseResult<?>) result;
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", baseResult.getCode());
                    map.put("message", baseResult.getMessage());
                    // 处理 data 字段，确保可以序列化
                    Object data = baseResult.getData();
                    if (data != null) {
                        try {
                            // 特殊处理 PageResult 类型
                            if (data instanceof PageResult) {
                                PageResult<?> pageResult = (PageResult<?>) data;
                                Map<String, Object> pageResultMap = new HashMap<>();
                                pageResultMap.put("items", pageResult.getItems());
                                pageResultMap.put("total", pageResult.getTotal());
                                map.put("data", pageResultMap);
                            } else {
                                // 其他类型，尝试序列化为 JSON 字符串再解析为 Map
                                String dataJson = objectMapper.writeValueAsString(data);
                                map.put("data", objectMapper.readValue(dataJson, Map.class));
                            }
                        } catch (Exception e3) {
                            // 如果序列化失败，尝试转换为 Map
                            try {
                                map.put("data", objectMapper.convertValue(data, Map.class));
                            } catch (Exception e4) {
                                // 最后降级：尝试直接序列化整个 data 对象
                                try {
                                    String dataJson = objectMapper.writeValueAsString(data);
                                    // 如果序列化成功，解析为 Object（可能是 Map 或 List）
                                    map.put("data", objectMapper.readValue(dataJson, Object.class));
                                } catch (Exception e5) {
                                    // 最后降级：使用类型名称
                                    log.warn("无法序列化 data 字段: {}, 类型: {}", e5.getMessage(), data.getClass().getName());
                                    map.put("data", "无法序列化: " + data.getClass().getSimpleName());
                                }
                            }
                        }
                    } else {
                        map.put("data", null);
                    }
                    return objectMapper.writeValueAsString(map);
                }
                // 其他类型，尝试转换为 Map 再序列化
                Map<String, Object> map = objectMapper.convertValue(result, Map.class);
                return objectMapper.writeValueAsString(map);
            } catch (Exception e2) {
                log.error("降级序列化也失败: {}", e2.getMessage());
                // 最后降级：返回错误信息的 JSON 字符串，而不是 toString()
                try {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("error", "序列化失败");
                    errorMap.put("type", result.getClass().getName());
                    errorMap.put("message", e2.getMessage());
                    return objectMapper.writeValueAsString(errorMap);
                } catch (Exception e3) {
                    // 如果连错误信息都无法序列化，返回最简单的 JSON
                    return "{\"error\":\"序列化失败\"}";
                }
            }
        }
    }

    /**
     * 获取真实姓名
     */
    private String getRealName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        try {
            SysUser user = userMapper.selectById(userId);
            return user != null ? user.getRealName() : null;
        } catch (Exception e) {
            log.debug("获取用户真实姓名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从登录请求中提取用户名
     */
    private String extractUsernameFromLoginRequest(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    if (arg != null) {
                        // 尝试通过反射获取 username 字段
                        try {
                            java.lang.reflect.Field field = arg.getClass().getDeclaredField("username");
                            field.setAccessible(true);
                            Object usernameValue = field.get(arg);
                            if (usernameValue != null) {
                                return usernameValue.toString();
                            }
                        } catch (NoSuchFieldException e) {
                            // 如果没有 username 字段，尝试其他方式
                        }
                        // 尝试将对象转换为 Map 或 JSON，然后提取 username
                        try {
                            String jsonStr = objectMapper.writeValueAsString(arg);
                            Map<String, Object> map = objectMapper.readValue(jsonStr, Map.class);
                            Object usernameValue = map.get("username");
                            if (usernameValue != null) {
                                return usernameValue.toString();
                            }
                        } catch (Exception e) {
                            log.debug("从登录请求中提取用户名失败: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取登录用户名失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
