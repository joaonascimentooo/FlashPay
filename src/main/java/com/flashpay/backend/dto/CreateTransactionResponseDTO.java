package com.flashpay.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTransactionResponseDTO {

    private String id;
    private String senderId;
    private String receiverId;
    private BigDecimal value;
    private LocalDateTime timestamp;
}
