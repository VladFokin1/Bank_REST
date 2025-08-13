package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User createUser(String username, String password, String role);
    User updateUser(Long userId, String newPassword, String newRole);
    void deleteUser(Long userId);
    Page<User> getAllUsers(Pageable pageable);
    User getUserById(Long userId);
    User findByUsername(String username);
}
