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

/**
 * Serviço responsável pela lógica de autenticação.
 *
 * 🔄 Responsabilidades:
 * 1. Registrar novo usuário (com validações)
 * 2. Fazer login (validar credenciais)
 * 3. Gerar token JWT
 * 4. Retornar dados do usuário autenticado
 *
 * 🔐 Segurança:
 * - Senha é criptografada com BCrypt
 * - Validação de documentos/emails duplicados
 * - Tratamento de erros seguro (não expõe detalhes)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    /**
     * Repositório de usuários (banco de dados)
     */
    private final UserRepository userRepository;

    /**
     * Gerenciador de autenticação do Spring Security
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Provider de tokens JWT
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Criptografador de senhas (BCrypt)
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Serviço que carrega usuário do banco
     */
    private final UserDetailsService userDetailsService;

    /**
     * Registra um novo usuário no sistema.
     *
     * 🔄 Fluxo:
     * 1. Validar se email já está registrado
     * 2. Validar se documento já está registrado
     * 3. Criptografar a senha
     * 4. Criar novo usuário no banco
     * 5. Gerar token JWT
     * 6. Retornar resposta com token
     *
     * @param registerRequest Dados fornecidos pelo usuário
     * @return Resposta com token JWT
     * @throws DuplicateResourceException Se email ou documento já existe
     */
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Iniciando registro de novo usuário: {}", registerRequest.getEmail());

        // 1️⃣ Validar email duplicado
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Tentativa de registrar email duplicado: {}", registerRequest.getEmail());
            throw new DuplicateResourceException("Email já está registrado no sistema");
        }

        // 2️⃣ Validar documento duplicado
        if (userRepository.findByUserDocument(registerRequest.getUserDocument()).isPresent()) {
            log.warn("Tentativa de registrar documento duplicado: {}", registerRequest.getUserDocument());
            throw new DuplicateResourceException("Documento já está registrado no sistema");
        }

        // 3️⃣ Criar novo usuário
        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setEmail(registerRequest.getEmail());
        
        // 🔐 IMPORTANTE: Criptografar a senha com BCrypt
        // Nunca armazenar senha em texto plano!
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        newUser.setUserDocument(registerRequest.getUserDocument());
        
        // Converter string para enum
        newUser.setUserType(UserType.valueOf(registerRequest.getUserType()));
        
        // Saldo inicial (default: 0 se não informado)
        BigDecimal balance = registerRequest.getBalance() != null 
            ? registerRequest.getBalance() 
            : BigDecimal.ZERO;
        newUser.setBalance(balance);

        // 4️⃣ Salvar no banco
        User savedUser = userRepository.save(newUser);
        log.info("Novo usuário registrado: {} - {}", savedUser.getId(), savedUser.getEmail());

        // 5️⃣ Carregar UserDetails e gerar token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        // 6️⃣ Retornar resposta
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

    /**
     * Faz login do usuário.
     *
     * 🔄 Fluxo:
     * 1. Carregar usuário do banco
     * 2. Validar credenciais (email + senha)
     * 3. Se válido, gerar token JWT
     * 4. Retornar resposta com token
     *
     * 🔐 Validação de credenciais:
     * - AuthenticationManager é responsável
     * - Usa BCrypt para comparar senhas
     * - Se falhar, lança BadCredentialsException
     *
     * @param loginRequest Email e senha do usuário
     * @return Resposta com token JWT
     * @throws UserNotFoundException Se usuário não existe
     * @throws BadCredentialsException Se senha está errada
     */
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Tentativa de login: {}", loginRequest.getEmail());

        try {
            // 1️⃣ Validar se usuário existe
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Usuário não encontrado: {}", loginRequest.getEmail());
                        return new UserNotFoundException("Email ou senha inválidos");
                    });

            // 2️⃣ Autenticar com AuthenticationManager
            // Ele valida email/senha usando BCrypt
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 3️⃣ Se chegou aqui, credenciais são válidas
            // Gerar token JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            log.info("Login bem-sucedido: {}", loginRequest.getEmail());

            // 4️⃣ Retornar resposta
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

    /**
     * Retorna dados do usuário autenticado.
     *
     * 🔐 Requer que o usuário já esteja autenticado
     * (token JWT válido no header)
     *
     * @return Dados do usuário autenticado
     * @throws UserNotFoundException Se usuário não encontrado
     */
    public AuthResponseDTO getCurrentUser() {
        // Pegar do SecurityContext (preenchido pelo JwtAuthenticationFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("Usuário não autenticado");
        }

        // Email está no UserDetails
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
