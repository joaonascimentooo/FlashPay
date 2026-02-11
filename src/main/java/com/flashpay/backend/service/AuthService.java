package com.flashpay.backend.service;

import com.flashpay.backend.domain.RefreshToken;
import com.flashpay.backend.domain.User;
import com.flashpay.backend.dto.AuthResponseDTO;
import com.flashpay.backend.dto.LoginRequestDTO;
import com.flashpay.backend.dto.RefreshTokenRequestDTO;
import com.flashpay.backend.dto.RegisterRequestDTO;
import com.flashpay.backend.enums.UserType;
import com.flashpay.backend.exceptions.DuplicateResourceException;
import com.flashpay.backend.exceptions.UserNotFoundException;
import com.flashpay.backend.repository.RefreshTokenRepository;
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
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;
  
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
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        
        saveRefreshToken(savedUser, refreshToken);

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .userType(savedUser.getUserType().name())
                .expiresIn(jwtTokenProvider.getExpirationTimeInSeconds())
                .refreshExpiresIn(jwtTokenProvider.getRefreshTokenExpirationTimeInSeconds())
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
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
            
            saveRefreshToken(user, refreshToken);

            log.info("Login bem-sucedido: {}", loginRequest.getEmail());

            return AuthResponseDTO.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .userType(user.getUserType().name())
                    .expiresIn(jwtTokenProvider.getExpirationTimeInSeconds())
                    .refreshExpiresIn(jwtTokenProvider.getRefreshTokenExpirationTimeInSeconds())
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

    public AuthResponseDTO refreshAccessToken(RefreshTokenRequestDTO refreshRequest) {
        log.info("Tentativa de renovação de token");

        if (!jwtTokenProvider.isRefreshTokenValid(refreshRequest.getRefreshToken())) {
            log.warn("Refresh token inválido ou expirado");
            throw new UserNotFoundException("Refresh token inválido ou expirado");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshRequest.getRefreshToken())
                .orElseThrow(() -> {
                    log.warn("Refresh token não encontrado no banco");
                    return new UserNotFoundException("Refresh token não encontrado");
                });

        if (storedToken.isExpired() || storedToken.getRevoked()) {
            log.warn("Refresh token foi revogado ou expirou");
            throw new UserNotFoundException("Refresh token inválido ou revogado");
        }

        User user = storedToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtTokenProvider.generateToken(userDetails);

        log.info("Token renovado com sucesso para usuário: {}", user.getEmail());

        return AuthResponseDTO.builder()
                .token(newAccessToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType().name())
                .expiresIn(jwtTokenProvider.getExpirationTimeInSeconds())
                .build();
    }

    public void logout(String refreshToken) {
        try {
            RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new UserNotFoundException("Refresh token não encontrado"));
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            
            log.info("Logout realizado com sucesso");
        } catch (Exception e) {
            log.warn("Erro ao fazer logout: {}", e.getMessage());
        }
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 7 dias
        refreshToken.setRevoked(false);
        
        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token salvo para usuário: {}", user.getEmail());
    }
}