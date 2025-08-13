package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void authenticate_ValidCredentials_ReturnsToken() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("test-token");

        // Act
        String token = authService.authenticate("user", "password");

        // Assert
        assertEquals("test-token", token);
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user", "password"));
    }

    @Test
    void register_NewUser_Success() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setUsername("newuser");
        savedUser.setPassword("encoded-password");
        when(userRepository.save(any())).thenReturn(savedUser);

        // Act
        authService.register("newuser", "password", "USER");

        // Assert
        verify(userRepository).save(any());
        verify(passwordEncoder).encode("password");
    }

    @Test
    void register_ExistingUser_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                authService.register("existing", "password", "USER"));
    }
}
