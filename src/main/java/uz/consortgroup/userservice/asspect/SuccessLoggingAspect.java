package uz.consortgroup.userservice.asspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class SuccessLoggingAspect {
    @AfterReturning(
            pointcut = "@annotation(uz.consortgroup.userservice.asspect.annotation.AspectAfterReturning) || @annotation(uz.consortgroup.userservice.asspect.annotation.AllAspect)",
            returning = "result"
    )
    public void logAfterReturning(Object result) {
        log.info("Method executed successfully. Result: {}", result);
    }
}
