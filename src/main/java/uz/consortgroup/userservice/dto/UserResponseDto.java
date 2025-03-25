package uz.consortgroup.userservice.dto;

import lombok.Data;
import uz.consortgroup.userservice.entity.UserStatus;
import uz.consortgroup.userservice.entity.UsersRole;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String workPlace;
    private String email;
    private String position;
    private String pinfl;
    private UsersRole usersRole;
    private UserStatus userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
