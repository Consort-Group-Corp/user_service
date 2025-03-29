package uz.consortgroup.userservice.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureCodeGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateSecureCode() {
        int code = 1000 + SECURE_RANDOM.nextInt(9000);
        return String.valueOf(code);
    }
}
