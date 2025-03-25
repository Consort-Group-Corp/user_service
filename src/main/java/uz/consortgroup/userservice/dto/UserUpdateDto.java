package uz.consortgroup.userservice.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String workPlace;
    private String email;
    private String position;
    private String pinfl;
}
