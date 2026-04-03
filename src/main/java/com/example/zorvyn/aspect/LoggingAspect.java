package com.example.zorvyn.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.example.zorvyn.service.impl.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        int argCount = joinPoint.getArgs().length;

        log.debug("[SERVICE] Entering {}.{} with {} arg(s)", className, methodName, argCount);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;
            log.debug("[SERVICE] Exiting {}.{} — took {}ms", className, methodName, elapsed);
            return result;
        } catch (Exception e) {
            log.error("[SERVICE] Exception in {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}

