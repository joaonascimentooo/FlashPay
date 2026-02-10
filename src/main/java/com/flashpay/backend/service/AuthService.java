package com.flashpay.backend.service;

import com.flashpay.backend.domain.User;
import com.flashpay.backend.dto.AuthResponseDTO;
import com.flashpay.backend.dto.LoginRequestDTO;
import com.flashpay.backend.dto.RegisterRequestDTO;
import com.flashpay.backend.enums.UserType;
import com.flashpay.backend.exceptions.DuplicateResourceException;
import com.flashpay.backend.exceptions.UserNotFoundException;
import com.flashpay.backend.repository.UserRepository;
import com.flashpay.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
  
    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final UserDetailsService userDetailsService;
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Iniciando registro de novo usuário: {}", registerRequest.getEmail());

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Tentativa de registrar email duplicado: {}", registerRequest.getEmail());
            throw new DuplicateResourceException("Email já está registrado no sistema");
        }

        if (userRepository.findByUserDocument(registerRequest.getUserDocument()).isPresent()) {
            log.warn("Tentativa de registrar documento duplicado: {}", registerRequest.getUserDocument());
            throw new DuplicateResourceException("Documento já está registrado no sistema");
        }

        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setUserDocument(registerRequest.getUserDocument());
        newUser.setUserType(UserType.valueOf(registerRequest.getUserType()));
        
        BigDecimal balance = registerRequest.getBalance() != null 
            ? registerRequest.getBalance() 
            : BigDecimal.ZERO;
        newUser.setBalance(balance);

        User savedUser = userRepository.save(newUser);
        log.info("Novo usuário registrado: {} - {}", savedUser.getId(), savedUser.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .userType(savedUser.getUserType().name())
                .expiresIn(jwtTokenProvider.getExpirationTimeInSeconds())
                .build();
    }

    
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Tentativa de login: {}", loginRequest.getEmail());

        try {
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Usuário não encontrado: {}", loginRequest.getEmail());
                        return new UserNotFoundException("Email ou senha inválidos");
                    });
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            log.info("Login bem-sucedido: {}", loginRequest.getEmail());

            return AuthResponseDTO.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .userType(user.getUserType().name())
                    .expiresIn(jwtTokenProvider.getExpirationTimeInSeconds())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Credenciais inválidas para: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Email ou senha inválidos");
        }
    }

    public AuthResponseDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("Usuário não autenticado");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType().name())
                .expiresIn(jwtTokenProvider.getExpirationTimeInSeconds())
                .build();
    }
}
