package com.flashpay.backend.service;

import com.flashpay.backend.domain.Transaction;
import com.flashpay.backend.domain.User;
import com.flashpay.backend.dto.CreateTransactionRequestDTO;
import com.flashpay.backend.dto.CreateTransactionResponseDTO;
import com.flashpay.backend.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final UserService userService;

    @Transactional
    public CreateTransactionResponseDTO createTransaction(CreateTransactionRequestDTO data) {
        log.info("Processing transaction from {} to {}", data.getSenderId(), data.getReceiverId());

        User sender = this.userService.findUserById(data.getSenderId());
        User receiver = this.userService.findUserById(data.getReceiverId());

        if (sender.getId().equals(receiver.getId())) {
            log.warn("Attempted transfer to same user: {}", sender.getId());
            throw new IllegalArgumentException("Não é permitido transferir para você mesmo");
        }

        this.userService.validateTransaction(sender, data.getValue());

        sender.setBalance(sender.getBalance().subtract(data.getValue()));
        receiver.setBalance(receiver.getBalance().add(data.getValue()));

        Transaction newTransaction = new Transaction();
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setAmount(data.getValue());
        newTransaction.setTimestamp(LocalDateTime.now());

        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);
        Transaction savedTransaction = this.transactionRepository.save(newTransaction);

        log.info("Transaction completed: {} - Amount: {}", savedTransaction.getId(), data.getValue());

        return new CreateTransactionResponseDTO(
                savedTransaction.getId(),
                savedTransaction.getSender().getId(),
                savedTransaction.getReceiver().getId(),
                savedTransaction.getAmount(),
                savedTransaction.getTimestamp()
        );
    }
}
