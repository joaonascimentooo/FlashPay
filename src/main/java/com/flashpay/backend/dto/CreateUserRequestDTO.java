package com.flashpay.backend.dto;

import com.flashpay.backend.enums.UserType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateUserRequestDTO {

    @NotBlank(message = "Primeiro nome não pode ser vazio")
    @Size(min = 2, max = 100, message = "Primeiro nome deve ter entre 2 e 100 caracteres")
    private String firstName;

    @NotBlank(message = "Sobrenome não pode ser vazio")
    @Size(min = 2, max = 100, message = "Sobrenome deve ter entre 2 e 100 caracteres")
    private String lastName;

    @NotBlank(message = "Documento não pode ser vazio")
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Documento deve ser CPF (11 dígitos) ou CNPJ (14 dígitos)")
    private String userDocument;

    @NotBlank(message = "Email não pode ser vazio")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotBlank(message = "Senha não pode ser vazia")
    @Size(min = 8, max = 50, message = "Senha deve ter entre 8 e 50 caracteres")
    private String password;

    @NotNull(message = "Saldo não pode ser nulo")
    @DecimalMin(value = "0.01", message = "Saldo deve ser maior que zero")
    private BigDecimal balance;

    @NotNull(message = "Tipo de usuário não pode ser nulo")
    private UserType userType;
}
