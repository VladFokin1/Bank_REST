package com.example.bankcards.service;

public interface AuthService {
    String authenticate(String username, String password);
    void register(String username, String password, String role);
}
