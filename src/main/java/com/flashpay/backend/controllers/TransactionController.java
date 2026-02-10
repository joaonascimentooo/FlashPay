package com.flashpay.backend.controllers;

import com.flashpay.backend.dto.CreateTransactionRequestDTO;
import com.flashpay.backend.dto.CreateTransactionResponseDTO;
import com.flashpay.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<CreateTransactionResponseDTO> createTransaction(@Valid @RequestBody CreateTransactionRequestDTO transaction) {
        CreateTransactionResponseDTO newTransaction = transactionService.createTransaction(transaction);
        return new ResponseEntity<>(newTransaction, HttpStatus.CREATED);
    }
}
