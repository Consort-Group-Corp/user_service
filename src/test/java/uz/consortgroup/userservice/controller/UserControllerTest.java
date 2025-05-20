package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserStatus;
import uz.consortgroup.core.api.v1.dto.user.request.UserProfileRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserRegistrationRequestDto;
import uz.consortgroup.core.api.v1.dto.user.request.UserUpdateRequestDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserProfileResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserRegistrationResponseDto;
import uz.consortgroup.core.api.v1.dto.user.response.UserUpdateResponseDto;
import uz.consortgroup.userservice.exception.InvalidVerificationCodeException;
import uz.consortgroup.userservice.exception.UserAlreadyExistsException;
import uz.consortgroup.userservice.exception.UserNotFoundException;
import uz.consortgroup.userservice.exception.VerificationCodeExpiredException;
import uz.consortgroup.userservice.service.user.UserServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserServiceImpl userServiceImpl;

    private final UUID testUserId = UUID.randomUUID();
    private final String BASE_URL = "/api/v1/users";

    @Test
    @WithMockUser
    void registerUser_Success() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setLanguage(Language.UZBEK);

        UserRegistrationResponseDto response = UserRegistrationResponseDto.builder()
                .id(testUserId)
                .email(request.getEmail())
                .language(request.getLanguage())
                .build();

        when(userServiceImpl.registerNewUser(any(UserRegistrationRequestDto.class))).thenReturn(response);

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
        doNothing().when(userServiceImpl).verifyUser(any(UUID.class), anyString());

        mockMvc.perform(post(BASE_URL + "/{userId}/verification", testUserId)
                        .param("verificationCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("User verified successfully"));
    }

    @Test
    @WithMockUser
    void resendVerificationCode_Success() throws Exception {
        doNothing().when(userServiceImpl).resendVerificationCode(any(UUID.class));

        mockMvc.perform(post(BASE_URL + "/{userId}/new-verification-code", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification code resent successfully"));
    }

    @Test
    @WithMockUser
    void fillUserProfile_Success() throws Exception {
        UserProfileRequestDto request = createValidProfileDto();
        UserProfileResponseDto response = UserProfileResponseDto.builder()
                .id(testUserId)
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .email("test@example.com")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(userServiceImpl.fillUserProfile(any(UUID.class), any(UserProfileRequestDto.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{userId}/profile", testUserId)  // Изменено с post на put
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

        when(userServiceImpl.getUserById(any(UUID.class))).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()));
    }

    @Test
    @WithMockUser
    void updateUser_Success() throws Exception {
        UserUpdateRequestDto request = createValidUpdateDto();
        UserUpdateResponseDto response = UserUpdateResponseDto.builder()
                .id(testUserId)
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        when(userServiceImpl.updateUserById(any(UUID.class), any(UserUpdateRequestDto.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()));
    }

    @Test
    @WithMockUser
    void deleteUser_Success() throws Exception {
        doNothing().when(userServiceImpl).deleteUserById(any(UUID.class));

        mockMvc.perform(delete(BASE_URL + "/{userId}", testUserId))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser
    void registerUser_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserRegistrationRequestDto invalidDto = new UserRegistrationRequestDto();
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
        UserRegistrationRequestDto validDto = new UserRegistrationRequestDto();
        validDto.setEmail("existing@example.com");
        validDto.setPassword("Password123!");
        validDto.setLanguage(Language.UZBEK);

        when(userServiceImpl.registerNewUser(any(UserRegistrationRequestDto.class)))
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
                .when(userServiceImpl).verifyUser(any(UUID.class), anyString());

        mockMvc.perform(post(BASE_URL + "/{userId}/verification", testUserId)
                        .param("verificationCode", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void verifyUser_ExpiredCode_ShouldReturnBadRequest() throws Exception {
        doThrow(new VerificationCodeExpiredException("Code expired"))
                .when(userServiceImpl).verifyUser(any(UUID.class), anyString());

        mockMvc.perform(post(BASE_URL + "/{userId}/verification", testUserId)
                        .param("verificationCode", "expired"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void verifyUser_MissingCode_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{userId}/verification", testUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter 'verificationCode' " +
                        "for method parameter type String is not present"));
    }

    @Test
    @WithMockUser
    void resendVerificationCode_UserNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found"))
                .when(userServiceImpl).resendVerificationCode(any(UUID.class));

        mockMvc.perform(post(BASE_URL + "/{userId}/new-verification-code", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void fillUserProfile_InvalidData_ShouldReturnBadRequest() throws Exception {
        UserProfileRequestDto invalidDto = new UserProfileRequestDto();
        invalidDto.setPhoneNumber("invalid");

        mockMvc.perform(put(BASE_URL + "/{userId}/profile", testUserId)  // Изменено с post на put
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void fillUserProfile_UserNotFound_ShouldReturnNotFound() throws Exception {
        UserProfileRequestDto validDto = createValidProfileDto();

        when(userServiceImpl.fillUserProfile(any(UUID.class), any(UserProfileRequestDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(put(BASE_URL + "/{userId}/profile", testUserId)  // Изменено с post на put
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUserById_NotFound_ShouldReturnNotFound() throws Exception {
        when(userServiceImpl.getUserById(any(UUID.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get(BASE_URL + "/{userId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateUser_InvalidRole_ShouldReturnBadRequest() throws Exception {
        UserUpdateRequestDto invalidDto = createValidUpdateDto();
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
                .when(userServiceImpl).deleteUserById(any(UUID.class));

        mockMvc.perform(delete(BASE_URL + "/{userId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private UserProfileRequestDto createValidProfileDto() {
        return UserProfileRequestDto.builder()
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

    private UserUpdateRequestDto createValidUpdateDto() {
        return UserUpdateRequestDto.builder()
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