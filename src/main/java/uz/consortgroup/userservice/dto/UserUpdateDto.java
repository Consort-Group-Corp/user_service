package uz.consortgroup.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import uz.consortgroup.userservice.entity.enumeration.UserRole;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserUpdateDto {

    private Long id;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String middleName;

    @Past(message = "Born date must be in the past")
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate bornDate;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+998\\d{9}$")
    private String phoneNumber;

    @NotBlank(message = "Work place is required")
    @Size(max = 128)
    private String workPlace;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Position is required")
    @Size(max = 128)
    private String position;

    @NotBlank(message = "Pinfl is required")
    @Size(max = 14)
    private String pinfl;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role is required")
    private UserRole role;
}
