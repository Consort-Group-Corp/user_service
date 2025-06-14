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

    @Value("${kafka.password-reset-request}")
    private String passwordResetRequestTopic;

    @Value("${kafka.super-admin-action}")
    private String superAdminActionTopic;

    @Value("${kafka.mentor-action}")
    private String mentorActionTopic;

    @Value("${kafka.course-group}")
    private String courseGroupTopic;

    @Value("${kafka.course-purchased}")
    private String coursePurchasedTopic;

    @Value("${kafka.hr-action}")
    private String hrActionTopic;
}
