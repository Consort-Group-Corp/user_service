package uz.consortgroup.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdatePasswordRequestDto {
    @NotBlank(message = "New password is required")
    private String newPassword;

    @NotBlank(message = "Reset token is required")
    private String confirmPassword;
}
