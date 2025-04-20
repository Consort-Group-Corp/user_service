package uz.consortgroup.userservice.dto.super_admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.userservice.entity.enumeration.UserRole;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserChangeRequestDto {
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "New role is required")
    private UserRole newRole;
}
