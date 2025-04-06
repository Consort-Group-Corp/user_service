package uz.consortgroup.user_service.asspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class ExceptionLoggingAspect {

    @AfterThrowing(pointcut = "@annotation(uz.consortgroup.user_service.asspect.annotation.AspectAfterThrowing)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String argsString = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));
        String exceptionName = ex.getClass().getSimpleName();
        String exceptionMessage = ex.getMessage();

        log.error("Exception in {}.{}({}): [{}] {}",
                className, methodName, argsString, exceptionName, exceptionMessage, ex);
    }
}
