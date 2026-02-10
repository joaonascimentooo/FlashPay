package com.flashpay.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    
    private String token;
    
    private String tokenType;
    
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String userType;
    
    private Long expiresIn;
}
