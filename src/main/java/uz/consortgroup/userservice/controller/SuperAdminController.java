package uz.consortgroup.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.userservice.dto.super_admin.UserChangeRequestDto;
import uz.consortgroup.userservice.dto.super_admin.UserCreateDto;
import uz.consortgroup.userservice.dto.super_admin.UserResponseDto;
import uz.consortgroup.userservice.service.SuperAdminService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/super-admin")
@Validated
public class SuperAdminController {
    private final SuperAdminService superAdminService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/new-role")
    public UserResponseDto findUserByEmailAndChangeUserRole(@RequestBody @Valid UserChangeRequestDto userChangeRequestDto) {
        return superAdminService.findUserByEmailAndChangeUserRole(userChangeRequestDto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/new-user")
    public UserResponseDto createNewUserWithMentorRole(@Valid @RequestBody UserCreateDto userCreateDto) {
        return superAdminService.createNewUserWithMentorRole(userCreateDto);
    }
}
