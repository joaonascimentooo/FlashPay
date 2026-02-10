package com.flashpay.backend.controllers;

import com.flashpay.backend.dto.CreateUserRequestDTO;
import com.flashpay.backend.dto.CreateUserResponseDTO;
import com.flashpay.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<CreateUserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO user){
        CreateUserResponseDTO newUser = userService.createUser(user);
        return  new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
}
