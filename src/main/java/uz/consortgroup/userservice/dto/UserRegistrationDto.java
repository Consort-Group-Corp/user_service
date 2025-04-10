package uz.consortgroup.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.userservice.entity.enumeration.Language;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRegistrationDto {
    @NotNull(message = "Language is required")
    private Language language;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "Password must contain at least one digit, one lowercase letter, and one uppercase letter")
    @NotBlank(message = "Password is required")
    private String password;
}
