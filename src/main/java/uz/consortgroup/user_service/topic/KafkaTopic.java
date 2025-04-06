package uz.consortgroup.user_service.topic;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KafkaTopic {
    @Value("${kafka.user-registration}")
    private String userRegisteredTopic;

    @Value("${kafka.verification-code-resent}")
    private String verificationCodeResentTopic;
}
