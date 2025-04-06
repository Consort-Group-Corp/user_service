package uz.consortgroup.user_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.user_service.entity.enumeration.UserRole;
import uz.consortgroup.user_service.entity.enumeration.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserUpdateResponseDto {
    private Long id;
    private String lastName;
    private String firstName;
    private String middleName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate bornDate;
    private String phoneNumber;
    private String workPlace;
    private String email;
    private String position;
    private String pinfl;
    private UserStatus status;
    private UserRole role;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
