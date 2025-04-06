package uz.consortgroup.user_service.asspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class SuccessLoggingAspect {
    @AfterReturning(pointcut = "@annotation(uz.consortgroup.user_service.asspect.annotation.AspectAfterReturning)", returning = "result")
    public void logAfterReturning(Object result) {
        log.info("Method executed successfully. Result: {}", result);
    }
}
