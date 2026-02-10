package com.flashpay.backend.security;

import com.flashpay.backend.domain.User;
import com.flashpay.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado com email: {}", email);
                    return new UsernameNotFoundException("Usuário não encontrado: " + email);
                });

        log.debug("Usuário encontrado: {}", email);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(getAuthoritiesForUser(user))
                .accountLocked(false)       
                .accountExpired(false)       
                .credentialsExpired(false)   
                .disabled(false)             
                
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthoritiesForUser(User user) {
        String authority = "ROLE_" + user.getUserType().name();
        return Collections.singleton(new SimpleGrantedAuthority(authority));
    }
}
