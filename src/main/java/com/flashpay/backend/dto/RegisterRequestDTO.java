package com.flashpay.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    private String firstName;
    
    @NotBlank(message = "Sobrenome é obrigatório")
    private String lastName;
    
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 128, message = "Senha deve ter entre 8 e 128 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[A-Za-z\\d@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,}$",
        message = "Senha deve conter: maiúscula, minúscula, número e caractere especial (@$!%*?&#^etc)"
    )
    private String password;
    
    @NotBlank(message = "CPF/CNPJ é obrigatório")
    private String userDocument;
    
    @NotBlank(message = "Tipo de usuário é obrigatório")
    private String userType; 
    
    private BigDecimal balance; 
}
