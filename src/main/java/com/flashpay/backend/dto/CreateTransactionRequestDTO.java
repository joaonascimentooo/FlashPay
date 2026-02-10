package com.flashpay.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateTransactionRequestDTO {

    @NotBlank(message = "ID do remetente não pode ser vazio")
    private String senderId;

    @NotBlank(message = "ID do destinatário não pode ser vazio")
    private String receiverId;

    @NotNull(message = "Valor não pode ser nulo")
    @DecimalMin(value = "0.01", message = "Valor da transferência deve ser maior que zero")
    private BigDecimal value;
}
