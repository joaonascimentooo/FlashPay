package com.flashpay.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateTransactionRequestDTO {

    private String senderId;
    private String receiverId;
    private BigDecimal value;
}
