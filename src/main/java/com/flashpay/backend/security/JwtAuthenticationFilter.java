package com.flashpay.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

   
    private final JwtTokenProvider jwtTokenProvider;

    private final UserDetailsService userDetailsService;

   
    private static final String BEARER_PREFIX = "Bearer ";

   
    private static final String HEADER_AUTHORIZATION = "Authorization";

   
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
           
            String token = extractTokenFromRequest(request);

            if (token != null && jwtTokenProvider.isTokenValid(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,          
                        null,                
                        userDetails.getAuthorities() 
                    );
                
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                log.debug("Autenticação bem-sucedida para usuário: {}", email);
            }
        } catch (Exception e) {
            log.warn("Não foi possível autenticar: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

   
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
