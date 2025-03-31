package uz.consortgroup.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationDto {
    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String middleName;

    @NotBlank(message = "Work place is required")
    @Size(max = 128)
    private String workPlace;

    @NotBlank(message = "Email is required")
    @Email
    @Size(max = 128)
    private String email;

    @NotBlank(message = "Position is required")
    @Size(max = 128)
    private String position;

    @NotBlank(message = "Pinfl is required")
    @Size(max = 14)
    private String pinfl;

    @NotBlank(message = "Password is required")
    private String password;
}
