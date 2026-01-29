package com.smartims.repository;

import com.smartims.entity.OtpVerification;
import com.smartims.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email, OtpPurpose purpose
    );
}
