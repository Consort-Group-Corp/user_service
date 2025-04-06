package uz.consortgroup.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.user_service.dto.*;
import uz.consortgroup.user_service.entity.enumeration.Language;
import uz.consortgroup.user_service.entity.enumeration.UserRole;
import uz.consortgroup.user_service.entity.enumeration.UserStatus;
import uz.consortgroup.user_service.exception.UserAlreadyExistsException;
import uz.consortgroup.user_service.exception.UserNotFoundException;
import uz.consortgroup.user_service.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";
    private static final String VERIFY_URL = BASE_URL + "/1/verify";
    private static final String RESEND_CODE_URL = BASE_URL + "/{userId}/new-verification-code";
    private static final String GET_USER_URL = BASE_URL + "/{userId}";
    private static final String UPDATE_USER_URL = BASE_URL + "/{userId}";
    private static final String DELETE_USER_URL = BASE_URL + "/{userId}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserRegistrationDto createValidRegistrationDto() {
        return UserRegistrationDto.builder()
                .language(Language.RUSSIAN)
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .bornDate(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .phoneNumber("+998901234567")
                .workPlace("Google")
                .email("ivan@gmail.com")
                .position("Developer")
                .pinfl("12345678901234")
                .password("SecurePass123!")
                .build();
    }

    private UserResponseDto createValidResponseDto() {
        return UserResponseDto.builder()
                .id(1L)
                .language(Language.RUSSIAN.getCode())
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .bornDate(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .phoneNumber("+998901234567")
                .workPlace("Google")
                .email("Ivan@gmail.com")
                .position("Developer")
                .role(UserRole.STUDENT)
                .status(UserStatus.PENDING)
                .createdAt(LocalDateTime.of(2023, 1, 1, 12, 0))
                .lastLoginAt(null)
                .build();
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void registerNewUser_Success() throws Exception {
        UserRegistrationDto request = createValidRegistrationDto();
        UserResponseDto response = createValidResponseDto();

        when(userService.registerNewUser(any(UserRegistrationDto.class))).thenReturn(response);

        mvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void registerNewUser_InvalidData() throws Exception {
        UserRegistrationDto request = createValidRegistrationDto();
        request.setEmail("");

        mvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void registerNewUser_ExistingEmail() throws Exception {
        when(userService.registerNewUser(any(UserRegistrationDto.class)))
                .thenThrow(new UserAlreadyExistsException("User with this email already exists"));

        mvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRegistrationDto())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User already exists"))
                .andExpect(jsonPath("$.message").value("User with this email already exists"));
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void verifyUser_Success() throws Exception {
        doNothing().when(userService).verifyUser(anyLong(), anyString());

        mvc.perform(post(VERIFY_URL)
                        .param("verificationCode", "123456"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void verifyUser_InvalidCode() throws Exception {
        mvc.perform(post(VERIFY_URL)
                        .param("verificationCode", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void resendVerificationCode_Success() throws Exception {
        doNothing().when(userService).resendVerificationCode(anyLong());

        mvc.perform(post(RESEND_CODE_URL, 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void resendVerificationCode_UserNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).resendVerificationCode(anyLong());

        mvc.perform(post(RESEND_CODE_URL, -1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void getUserById_Success() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(UserResponseDto.builder().build());

        mvc.perform(get(GET_USER_URL, 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new UserNotFoundException("User not found"));

        mvc.perform(get(GET_USER_URL, -1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void updateUserById_Success() throws Exception {
        UserUpdateDto request = UserUpdateDto.builder()
                .id(1L)
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .bornDate(LocalDate.of(1995, 5, 15))
                .phoneNumber("+998123456789")
                .pinfl("1234567890")
                .workPlace("Google")
                .position("Developer")
                .email("Ivan@gmail.com")
                .role(UserRole.ANALYST)
                .build();

        when(userService.updateUserById(anyLong(), any(UserUpdateDto.class)))
                .thenReturn(new UserUpdateResponseDto());

        mvc.perform(put(UPDATE_USER_URL, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void updateUser_InvalidRole() throws Exception {
        String invalidJson = """
        {
            "firstName": "Ivan",
            "lastName": "Ivanov",
            "role": "INVALID_ROLE"
        }
        """;

        mvc.perform(put(UPDATE_USER_URL, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Valid roles are")));
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void updateUser_NullRole() throws Exception {
        String jsonWithNullRole = """
        {
            "firstName": "Ivan",
            "lastName": "Ivanov",
            "role": null
        }
        """;

        mvc.perform(put(UPDATE_USER_URL, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithNullRole))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void deleteUserById_Success() throws Exception {
        doNothing().when(userService).deleteUserById(anyLong());

        mvc.perform(delete(DELETE_USER_URL, 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "Ivan", roles = "STUDENT")
    void deleteUserById_NotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteUserById(anyLong());

        mvc.perform(delete(DELETE_USER_URL, -1L))
                .andExpect(status().isNotFound());
    }
}