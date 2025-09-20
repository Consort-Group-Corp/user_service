package uz.consortgroup.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uz.consortgroup.core.api.v1.dto.user.enumeration.Language;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserChangeRequestDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserCreateDto;
import uz.consortgroup.core.api.v1.dto.user.super_admin.UserResponseDto;
import uz.consortgroup.userservice.service.super_admin.SuperAdminService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SuperAdminControllerTest {

    @Mock
    private SuperAdminService superAdminService;

    @InjectMocks
    private SuperAdminController superAdminController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findUserByEmailAndChangeUserRole_shouldReturnOkWithValidRequest() throws Exception {
        UserChangeRequestDto request = new UserChangeRequestDto();
        request.setEmail("test@example.com");
        request.setNewRole(UserRole.MENTOR);

        UserResponseDto response = new UserResponseDto();
        when(superAdminService.findUserByEmailAndChangeUserRole(any())).thenReturn(response);

        mockMvc = MockMvcBuilders.standaloneSetup(superAdminController).build();
        mockMvc.perform(post("/api/v1/super-admin/new-role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void findUserByEmailAndChangeUserRole_shouldReturn400WhenEmailMissing() throws Exception {
        UserChangeRequestDto request = new UserChangeRequestDto();
        request.setNewRole(UserRole.MENTOR);

        mockMvc = MockMvcBuilders.standaloneSetup(superAdminController).build();
        mockMvc.perform(post("/api/v1/super-admin/new-role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findUserByEmailAndChangeUserRole_shouldReturn400WhenRoleMissing() throws Exception {
        UserChangeRequestDto request = new UserChangeRequestDto();
        request.setEmail("test@example.com");

        mockMvc = MockMvcBuilders.standaloneSetup(superAdminController).build();
        mockMvc.perform(post("/api/v1/super-admin/new-role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNewUserWithMentorRole_shouldReturnCreatedWithValidRequest() throws Exception {
        UserCreateDto request = getUserCreateDto();

        UserResponseDto response = new UserResponseDto();
        when(superAdminService.createNewUser(any())).thenReturn(response);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(superAdminController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();

        mockMvc.perform(post("/api/v1/super-admin/new-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    void createNewUser_shouldReturn400WhenEmailMissing() throws Exception {
        UserCreateDto request = new UserCreateDto();
        request.setFirstName("John");
        request.setLastName("Doe");

        mockMvc = MockMvcBuilders.standaloneSetup(superAdminController).build();
        mockMvc.perform(post("/api/v1/super-admin/new-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNewUser_shouldReturn400WhenFirstNameMissing() throws Exception {
        UserCreateDto request = new UserCreateDto();
        request.setEmail("mentor@example.com");
        request.setLastName("Doe");

        mockMvc = MockMvcBuilders.standaloneSetup(superAdminController).build();
        mockMvc.perform(post("/api/v1/super-admin/new-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static UserCreateDto getUserCreateDto() {
        UserCreateDto request = new UserCreateDto();
        request.setLanguage(Language.ENGLISH);
        request.setLastName("Doe");
        request.setFirstName("John");
        request.setMiddleName("Middle");
        request.setBornDate(LocalDate.of(1990, 1, 1));
        request.setPhoneNumber("+998901234567");
        request.setWorkPlace("Company");
        request.setEmail("mentor@example.com");
        request.setPosition("Developer");
        request.setPinfl("12345678901234");
        request.setPassword("SecurePass123!");
        request.setRole(UserRole.MENTOR);
        return request;
    }
}