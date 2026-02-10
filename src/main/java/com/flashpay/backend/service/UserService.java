package com.flashpay.backend.service;

import com.flashpay.backend.domain.User;
import com.flashpay.backend.dto.CreateUserRequestDTO;
import com.flashpay.backend.dto.CreateUserResponseDTO;
import com.flashpay.backend.enums.UserType;
import com.flashpay.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void validateTransaction(User sender, BigDecimal amount) throws Exception{
        if (sender.getUserType() == UserType.SHOPKEEPER){
            throw new Exception("Lojistas não podem realizar transferências");
        }

        if (sender.getBalance().compareTo(amount) < 0 ){
            throw new Exception("Saldo Insuficiente");
        }
    }
    public User findUserById(String id) throws Exception{
        return this.userRepository.findById(id).orElseThrow(() -> new Exception("Usuario não encontrado"));
    }
    public void saveUser(User user){
        this.userRepository.save(user);
    }

    public CreateUserResponseDTO createUser(CreateUserRequestDTO data){
        User newUser = new User();
        newUser.setFirstName(data.getFirstName());
        newUser.setLastName(data.getLastName());
        newUser.setUserDocument(data.getUserDocument());
        newUser.setEmail(data.getEmail());
        newUser.setPassword(data.getPassword());
        newUser.setUserType(data.getUserType());
        newUser.setBalance(data.getBalance());

        User savedUser = this.userRepository.save(newUser);

        return new CreateUserResponseDTO(savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(), savedUser.getEmail(), savedUser.getUserType());
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }
}
