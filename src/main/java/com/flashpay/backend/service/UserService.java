package com.flashpay.backend.service;

import com.flashpay.backend.domain.User;
import com.flashpay.backend.dto.CreateUserRequestDTO;
import com.flashpay.backend.dto.CreateUserResponseDTO;
import com.flashpay.backend.enums.UserType;
import com.flashpay.backend.exceptions.DuplicateResourceException;
import com.flashpay.backend.exceptions.InsufficientBalanceException;
import com.flashpay.backend.exceptions.UserNotFoundException;
import com.flashpay.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

      public void validateTransaction(User sender, BigDecimal amount) {
        if (sender.getUserType() == UserType.SHOPKEEPER) {
            log.warn("Attempted transfer by shopkeeper: {}", sender.getId());
            throw new IllegalArgumentException("Lojistas não podem realizar transferências");
        }


       if (sender.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance for user: {}. Required: {}, Available: {}", 
                    sender.getId(), amount, sender.getBalance());
            throw new InsufficientBalanceException("Saldo Insuficiente");
        }
    }
     public User findUserById(String id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", id);
                    return new UserNotFoundException("Usuario não encontrado");
                });
    }
    public void saveUser(User user){
        this.userRepository.save(user);
    }

    public CreateUserResponseDTO createUser(CreateUserRequestDTO data) {
        if (userRepository.findByUserDocument(data.getUserDocument()).isPresent()) {
            log.warn("Attempt to register duplicate document: {}", data.getUserDocument());
            throw new DuplicateResourceException("Documento já está registrado no sistema");
        }

        if (userRepository.findByEmail(data.getEmail()).isPresent()) {
            log.warn("Attempt to register duplicate email: {}", data.getEmail());
            throw new DuplicateResourceException("Email já está registrado no sistema");
        }

        User newUser = new User();
        newUser.setFirstName(data.getFirstName());
        newUser.setLastName(data.getLastName());
        newUser.setUserDocument(data.getUserDocument());
        newUser.setEmail(data.getEmail());
        newUser.setPassword(data.getPassword());
        newUser.setUserType(data.getUserType());
        newUser.setBalance(data.getBalance());

        User savedUser = this.userRepository.save(newUser);
        log.info("New user created: {} - {}", savedUser.getId(), savedUser.getEmail());

        return new CreateUserResponseDTO(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getUserType());
    }
}
