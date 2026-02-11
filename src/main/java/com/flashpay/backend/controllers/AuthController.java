package com.flashpay.backend.controllers;

import com.flashpay.backend.dto.AuthResponseDTO;
import com.flashpay.backend.dto.LoginRequestDTO;
import com.flashpay.backend.dto.RefreshTokenRequestDTO;
import com.flashpay.backend.dto.RegisterRequestDTO;
import com.flashpay.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getCurrentUser() {
        AuthResponseDTO response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
   
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is working");
    }
   
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("Nova requisição de registro: {}", registerRequest.getEmail());
        
        try {
            AuthResponseDTO response = authService.register(registerRequest);
            log.info("Usuário registrado com sucesso: {}", registerRequest.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Erro no registro: {}", e.getMessage());
            throw e; 
        }
    }
   
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Nova requisição de login: {}", loginRequest.getEmail());
        
        try {
            AuthResponseDTO response = authService.login(loginRequest);
            log.info("Login bem-sucedido: {}", loginRequest.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro no login: {}", e.getMessage());
            throw e; 
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        log.info("Requisição de renovação de token");
        
        try {
            AuthResponseDTO response = authService.refreshAccessToken(refreshRequest);
            log.info("Token renovado com sucesso");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao renovar token: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        log.info("Requisição de logout");
        
        try {
            authService.logout(refreshRequest.getRefreshToken());
            log.info("Logout realizado com sucesso");
            
            return ResponseEntity.ok("Logout realizado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao fazer logout: {}", e.getMessage());
            throw e;
        }
    }
}