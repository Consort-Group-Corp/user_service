package uz.consortgroup.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.userservice.dto.super_admin.UserCreateDto;
import uz.consortgroup.userservice.dto.super_admin.UserResponseDto;
import uz.consortgroup.userservice.entity.User;
import uz.consortgroup.userservice.mapper.UserMapper;
import uz.consortgroup.userservice.service.operation.UserOperationsService;

@Service
@RequiredArgsConstructor
public class SuperAdminService {
    private final UserOperationsService userOperationsService;
    private final UserMapper userMapper;
    private final PasswordService passwordService;

    public UserResponseDto findUserByEmail(String email) {
        User user = userOperationsService.findUserByEmail(email);
        return userMapper.toUserResponseDto(user);
    }

    public UserResponseDto createNewUserWithMentorRole(UserCreateDto userCreateDto) {

        User user = User.builder()
                .language(userCreateDto.getLanguage())
                .lastName(userCreateDto.getLastName())
                .firstName(userCreateDto.getFirstName())
                .middleName(userCreateDto.getMiddleName())
                .bornDate(userCreateDto.getBornDate())
                .phoneNumber(userCreateDto.getPhoneNumber())
                .workPlace(userCreateDto.getWorkPlace())
                .email(userCreateDto.getEmail())
                .position(userCreateDto.getPosition())
                .pinfl(userCreateDto.getPinfl())
                .role(userCreateDto.getRole())
                .build();

        userOperationsService.saveUser(user);
        passwordService.savePassword(user, userCreateDto.getPassword());

        return userMapper.toUserResponseDto(user);
    }
}
