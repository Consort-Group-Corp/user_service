package uz.consortgroup.userservice.asspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RepositoryLoggingAspect {

    @Around("@within(org.springframework.stereotype.Repository)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            log.info("Around: Repository: {}, Method: {} execution time: {} ms", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), end - start);
            return result;
        } catch (Exception ex) {
            throw ex;
        }
    }
}
