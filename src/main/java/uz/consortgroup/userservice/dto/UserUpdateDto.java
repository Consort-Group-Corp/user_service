package uz.consortgroup.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.entity.enumeration.UsersRole;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserUpdateDto {
    private String lastName;
    private String firstName;
    private String middleName;
    private String workPlace;
    private String email;
    private String position;
    private String pinfl;
    private UserStatus userStatus;
    private UsersRole usersRole;
    private LocalDateTime updatedAt;
}
