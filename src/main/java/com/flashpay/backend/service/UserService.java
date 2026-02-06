package com.flashpay.backend.service;

import com.flashpay.backend.domain.User;
import com.flashpay.backend.enums.UserType;
import com.flashpay.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void validateTransaction(User sender, BigDecimal amount) throws Exception{
        if (sender.getUserType() == UserType.SHOPKEEPER){
            throw new Exception("Lojistas não podem realizar transferencias");
        }

        if (sender.getBalance().compareTo(amount) <0 ){
            throw new Exception("Saldo Insuficiente");
        }
    }
    public User findUserById(String id) throws Exception{
        return this.userRepository.findById(id).orElseThrow(() -> new Exception("Usuario não encontrado"));
    }
    public void saveUser(User user){
        this.userRepository.save(user);
    }
}
