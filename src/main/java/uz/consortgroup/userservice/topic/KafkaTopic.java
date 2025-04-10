package uz.consortgroup.userservice.topic;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KafkaTopic {
    @Value("${kafka.user-registration}")
    private String userRegistrationTopic;

    @Value("${kafka.user-update-profile}")
    private String userUpdateProfileTopic;

    @Value("${kafka.verification-code-resent}")
    private String verificationCodeResentTopic;
}
