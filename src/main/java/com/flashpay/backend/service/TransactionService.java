package com.flashpay.backend.service;

import com.flashpay.backend.domain.Transaction;
import com.flashpay.backend.domain.User;
import com.flashpay.backend.dto.CreateTransactionRequestDTO;
import com.flashpay.backend.dto.CreateTransactionResponseDTO;
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

    private final UserService userService;

    @Transactional
    public CreateTransactionResponseDTO createTransaction(CreateTransactionRequestDTO data) throws Exception{
        User sender = this.userService.findUserById(data.getSenderId());
        User receiver = this.userService.findUserById(data.getReceiverId());

        this.userService.validateTransaction(sender,data.getValue());

        sender.setBalance(sender.getBalance().subtract(data.getValue()));
        receiver.setBalance(receiver.getBalance().add(data.getValue()));

        Transaction newTransaction = new Transaction();
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setAmount(data.getValue());
        newTransaction.setTimestamp(LocalDateTime.now());

        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);
        this.transactionRepository.save(newTransaction);

        return new CreateTransactionResponseDTO(
                newTransaction.getId(),
                newTransaction.getAmount(),
                newTransaction.getTimestamp()
        );
    }
}
