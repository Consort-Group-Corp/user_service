package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.userservice.dto.*;
import uz.consortgroup.userservice.entity.enumeration.Language;
import uz.consortgroup.userservice.entity.enumeration.UserRole;
import uz.consortgroup.userservice.entity.enumeration.UserStatus;
import uz.consortgroup.userservice.exception.*;
import uz.consortgroup.userservice.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final UUID testUserId = UUID.randomUUID();
    private final String BASE_URL = "/api/v1/users";

    @Test
    @WithMockUser
    void registerUser_Success() throws Exception {
        UserRegistrationDto request = new UserRegistrationDto();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setLanguage(Language.UZBEK);

        UserRegistrationResponseDto response = UserRegistrationResponseDto.builder()
                .id(testUserId)
                .email(request.getEmail())
                .language(request.getLanguage())
                .build();

        when(userService.registerNewUser(any(UserRegistrationDto.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void verifyUser_Success() throws Exception {
        doNothing().when(userService).verifyUser(any(UUID.class), anyString());

        mockMvc.perform(post(BASE_URL + "/{userId}/verify", testUserId)
                        .param("verificationCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("User verified successfully"));
    }

    @Test
    @WithMockUser
    void resendVerificationCode_Success() throws Exception {
        doNothing().when(userService).resendVerificationCode(any(UUID.class));

        mockMvc.perform(post(BASE_URL + "/{userId}/new-verification-code", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification code resent successfully"));
    }

    @Test
    @WithMockUser
    void fillUserProfile_Success() throws Exception {
        UserProfileDto request = createValidProfileDto();
        UserProfileResponseDto response = UserProfileResponseDto.builder()
                .id(testUserId)
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.fillUserProfile(any(UUID.class), any(UserProfileDto.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{userId}/profile", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testUserId.toString()));
    }

    @Test
    @WithMockUser
    void getUserById_Success() throws Exception {
        UserProfileResponseDto response = UserProfileResponseDto.builder()
                .id(testUserId)
                .lastName("Doe")
                .firstName("John")
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(any(UUID.class))).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()));
    }

    @Test
    @WithMockUser
    void updateUser_Success() throws Exception {
        UserUpdateDto request = createValidUpdateDto();
        UserUpdateResponseDto response = UserUpdateResponseDto.builder()
                .id(testUserId)
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        when(userService.updateUserById(any(UUID.class), any(UserUpdateDto.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()));
    }

    @Test
    @WithMockUser
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUserById(any(UUID.class));

        mockMvc.perform(delete(BASE_URL + "/{userId}", testUserId))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser
    void registerUser_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserRegistrationDto invalidDto = new UserRegistrationDto();
        invalidDto.setEmail("invalid-email");
        invalidDto.setPassword("Password123!");
        invalidDto.setLanguage(Language.UZBEK);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void registerUser_ExistingEmail_ShouldReturnConflict() throws Exception {
        UserRegistrationDto validDto = new UserRegistrationDto();
        validDto.setEmail("existing@example.com");
        validDto.setPassword("Password123!");
        validDto.setLanguage(Language.UZBEK);

        when(userService.registerNewUser(any(UserRegistrationDto.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void verifyUser_InvalidCode_ShouldReturnBadRequest() throws Exception {
        doThrow(new InvalidVerificationCodeException("Invalid code"))
                .when(userService).verifyUser(any(UUID.class), anyString());

        mockMvc.perform(post(BASE_URL + "/{userId}/verify", testUserId)
                        .param("verificationCode", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void verifyUser_ExpiredCode_ShouldReturnBadRequest() throws Exception {
        doThrow(new VerificationCodeExpiredException("Code expired"))
                .when(userService).verifyUser(any(UUID.class), anyString());

        mockMvc.perform(post(BASE_URL + "/{userId}/verify", testUserId)
                        .param("verificationCode", "expired"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void verifyUser_MissingCode_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{userId}/verify", testUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter 'verificationCode' " +
                        "for method parameter type String is not present"));
    }

    @Test
    @WithMockUser
    void resendVerificationCode_UserNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).resendVerificationCode(any(UUID.class));

        mockMvc.perform(post(BASE_URL + "/{userId}/new-verification-code", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void fillUserProfile_InvalidData_ShouldReturnBadRequest() throws Exception {
        UserProfileDto invalidDto = new UserProfileDto();
        invalidDto.setPhoneNumber("invalid");

        mockMvc.perform(post(BASE_URL + "/{userId}/profile", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void fillUserProfile_UserNotFound_ShouldReturnNotFound() throws Exception {
        UserProfileDto validDto = createValidProfileDto();

        when(userService.fillUserProfile(any(UUID.class), any(UserProfileDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post(BASE_URL + "/{userId}/profile", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUserById_NotFound_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(any(UUID.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get(BASE_URL + "/{userId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateUser_InvalidRole_ShouldReturnBadRequest() throws Exception {
        UserUpdateDto invalidDto = createValidUpdateDto();
        invalidDto.setRole(null);

        mockMvc.perform(put(BASE_URL + "/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deleteUser_UserNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteUserById(any(UUID.class));

        mockMvc.perform(delete(BASE_URL + "/{userId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private UserProfileDto createValidProfileDto() {
        return UserProfileDto.builder()
                .lastName("Doe")
                .firstName("John")
                .middleName("Middle")
                .bornDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("+998901234567")
                .workPlace("Company")
                .position("Developer")
                .pinfl("12345678901234")
                .build();
    }

    private UserUpdateDto createValidUpdateDto() {
        return UserUpdateDto.builder()
                .lastName("Doe")
                .firstName("John")
                .middleName("Middle")
                .bornDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("+998901234567")
                .workPlace("Company")
                .email("test@example.com")
                .position("Developer")
                .pinfl("12345678901234")
                .role(UserRole.STUDENT)
                .build();
    }
}