package com.smartims.scheduler;

import com.smartims.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpVerificationRepository otpRepo;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // every hour
    public void cleanupOtps() {

        otpRepo.deleteExpiredOtps(LocalDateTime.now());
        otpRepo.deleteOldVerifiedOtps(LocalDateTime.now().minusDays(1));
    }
}

