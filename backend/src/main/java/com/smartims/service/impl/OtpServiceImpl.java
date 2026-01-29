package com.smartims.service.impl;

import com.smartims.entity.OtpVerification;
import com.smartims.enums.OtpPurpose;
import com.smartims.service.OtpException;
import com.smartims.repository.OtpVerificationRepository;
import com.smartims.service.EmailService;
import com.smartims.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepo;
    private final EmailService emailService;

    @Override
    public void generateAndSendOtp(String email, OtpPurpose purpose) {

        otpRepo.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .ifPresent(lastOtp -> {
                    if (lastOtp.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(30))) {
                        throw new RuntimeException("Please wait before requesting another OTP");
                    }
                });

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        OtpVerification entity = new OtpVerification();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setPurpose(purpose);
        entity.setVerified(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(10));

        otpRepo.save(entity);
        emailService.sendOtpEmail(email, otp, purpose);
    }

    @Override
    public void verifyOtp(String email, String otp, OtpPurpose purpose) {

        OtpVerification record = otpRepo
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new OtpException("OTP not found"));

        if (record.isVerified()) {
            throw new OtpException("OTP already used");
        }

        if (record.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpException("OTP expired");
        }

        if (!record.getOtp().equals(otp)) {
            throw new OtpException("Invalid OTP");
        }

        record.setVerified(true);
        otpRepo.save(record);
    }

    @Override
    public boolean isOtpVerified(String email, OtpPurpose purpose) {
        return otpRepo
                .findTopByEmailAndPurposeAndVerifiedTrueOrderByCreatedAtDesc(email, purpose)
                .isPresent();
    }

}
