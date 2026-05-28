package com.juzi.chien.admin.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juzi.chien.admin.common.BusinessType;
import com.juzi.chien.admin.common.Log;
import com.juzi.chien.admin.domain.entity.SysOperLog;
import com.juzi.chien.admin.security.LoginUser;
import com.juzi.chien.admin.service.SysOperLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 * 拦截 @Log 注解的方法，自动记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysOperLogService operLogService;
    private final ObjectMapper objectMapper;

    /**
     * 正常返回后记录日志
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object result) {
        handleLog(joinPoint, controllerLog, null, result);
    }

    /**
     * 异常后记录日志
     */
    @AfterThrowing(pointcut = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    private void handleLog(JoinPoint joinPoint, Log controllerLog, Exception exception, Object result) {
        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setOperTime(LocalDateTime.now());
            operLog.setTitle(controllerLog.title());
            operLog.setBusinessType(controllerLog.businessType().getCode());

            // 请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setOperUrl(request.getRequestURI());
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperIp(getClientIp(request));
            }

            // 方法信息
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");

            // 操作人信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                operLog.setOperName(loginUser.getUsername());
            }

            // 请求参数
            try {
                String params = objectMapper.writeValueAsString(joinPoint.getArgs());
                // 参数过长时截断
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                operLog.setOperParam(params);
            } catch (Exception e) {
                operLog.setOperParam("参数序列化失败");
            }

            // 返回结果
            if (result != null) {
                try {
                    String json = objectMapper.writeValueAsString(result);
                    if (json.length() > 2000) {
                        json = json.substring(0, 2000) + "...";
                    }
                    operLog.setJsonResult(json);
                } catch (Exception e) {
                    operLog.setJsonResult("结果序列化失败");
                }
            }

            // 状态和错误信息
            if (exception != null) {
                operLog.setStatus(0);
                String errorMsg = exception.getMessage();
                operLog.setErrorMsg(errorMsg != null && errorMsg.length() > 2000
                        ? errorMsg.substring(0, 2000) : errorMsg);
            } else {
                operLog.setStatus(1);
            }

            // 异步写入数据库
            operLogService.insertLog(operLog);

        } catch (Exception e) {
            log.error("记录操作日志失败: ", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
