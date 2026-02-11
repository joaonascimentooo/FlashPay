package com.flashpay.backend.service;

import com.flashpay.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        try {
            log.info("Iniciando limpeza de tokens expirados...");
            refreshTokenRepository.deleteExpiredTokens();
            log.info("Limpeza de tokens expirados concluida com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao limpar tokens expirados: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldRevokedTokens() {
        try {
            log.info("Iniciando limpeza de tokens revogados antigos...");
            refreshTokenRepository.deleteRevokedTokens();
            log.info("Limpeza de tokens revogados concluida com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao limpar tokens revogados: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 */6 * * * *")
    public void logTokenStatistics() {
        try {
            log.info("Estatisticas de tokens do sistema");
        } catch (Exception e) {
            log.error("Erro ao logar estatisticas: {}", e.getMessage());
        }
    }
}
