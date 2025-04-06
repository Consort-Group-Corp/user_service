package uz.consortgroup.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.userservice.entity.enumeration.Language;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationDto {
    @NotNull(message = "Language is required")
    private Language language;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String middleName;

    @NotNull(message = "Born date is required")
    @Past(message = "Born date must be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate bornDate;

    @NotBlank(message = "Phone number is required")
    @Size(max = 13)
    @Pattern(regexp = "^\\+998\\d{9}$")
    private String phoneNumber;

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

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "Password must contain at least one digit, one lowercase letter, and one uppercase letter")
    @NotBlank(message = "Password is required")
    private String password;
}
