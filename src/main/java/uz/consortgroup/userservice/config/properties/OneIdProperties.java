package uz.consortgroup.userservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "oneid")
public class OneIdProperties {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUrl;
    private String profileUrl;
}
