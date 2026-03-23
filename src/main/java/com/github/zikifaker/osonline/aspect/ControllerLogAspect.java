package com.github.zikifaker.osonline.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class ControllerLogAspect {

    private final static Logger logger = LoggerFactory.getLogger(ControllerLogAspect.class);

    // 定义一个切点表达式, 拦截controller层
    @Pointcut("execution(* com.github.zikifaker.osonline.controller..*.*(..))")
    public void controllerRequestPointCut() {
    }

    // 记录请求
    @Before("controllerRequestPointCut()")
    public void logRequest(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        logger.debug("\n=== 请求信息 ===\n" +
                        "IP: {}\n" +
                        "URL: {}\n" +
                        "HTTP Method: {}\n" +
                        "Class Method: {}.{}\n" +
                        "Params: {}",
                getClientIP(request),
                request.getRequestURL(),
                request.getMethod(),
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs())
        );
    }

    // 记录响应
    @AfterReturning(pointcut = "controllerRequestPointCut()", returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        logger.debug("\n=== 响应内容 ===\n{}", result);
    }

    // 统计接口执行时间
    @Around("controllerRequestPointCut()")
    public Object logAPIExecuteTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        // 执行目标方法
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        logger.debug("\n=== 接口耗时 === \n{} ms", duration);
        return result;
    }

    // 获取客户端真实ip
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
