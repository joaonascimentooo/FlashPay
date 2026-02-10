package com.flashpay.backend.service;

import com.flashpay.backend.domain.Transaction;
import com.flashpay.backend.domain.User;
import com.flashpay.backend.repository.TransactionRepository;
import com.flashpay.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    @Transactional
    public Transaction createTransaction (String senderId, String receiverId, BigDecimal value) throws Exception{
        User sender = this.userService.findUserById(senderId);
        User receiver = this.userService.findUserById(receiverId);

        this.userService.validateTransaction(sender,value);

        sender.setBalance(sender.getBalance().subtract(value));
        receiver.setBalance(receiver.getBalance().add(value));

        Transaction newTransaction = new Transaction();
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setAmount(value);
        newTransaction.setTimestamp(LocalDateTime.now());

        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);
        this.transactionRepository.save(newTransaction);

        return newTransaction;
    }
}
