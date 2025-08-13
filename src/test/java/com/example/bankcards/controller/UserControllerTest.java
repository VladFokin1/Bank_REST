package com.example.bankcards.controller;

import com.example.bankcards.controller.UserController;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Вспомогательный метод для создания тестового User
    private User createTestUser(Long id, String username, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_AdminAccess_ReturnsUsers() throws Exception {
        // 1. Создаем тестовую сущность User
        User user = createTestUser(1L, "admin", Role.ADMIN);

        // 2. Создаем Page<User>
        Page<User> page = new PageImpl<>(Collections.singletonList(user));

        // 3. Мокаем возврат Page<User>
        when(userService.getAllUsers(any())).thenReturn(page);

        // Выполнение и проверка
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("admin"))
                .andExpect(jsonPath("$.content[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_UserAccess_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        // 1. Создаем тестовую сущность User
        User newUser = createTestUser(2L, "newuser", Role.USER);

        // 2. Мокаем возврат User
        when(userService.createUser(any(), any(), any())).thenReturn(newUser);

        // Выполнение и проверка
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_AdminAccess_ReturnsUser() throws Exception {
        // 1. Создаем тестовую сущность User
        User user = createTestUser(1L, "admin", Role.ADMIN);

        // 2. Мокаем возврат User
        when(userService.getUserById(1L)).thenReturn(user);

        // Выполнение и проверка
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_AdminAccess_Success() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}