package com.smartims.repository;

import com.smartims.entity.OtpVerification;
import com.smartims.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email, OtpPurpose purpose
    );

    Optional<OtpVerification> findTopByEmailAndPurposeAndVerifiedTrueOrderByCreatedAtDesc(
            String email, OtpPurpose purpose
    );

//    Optional<OtpVerification>
//    findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
//            String email,
//            OtpPurpose purpose
//    );


    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiryTime < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.verified = true AND o.createdAt < :time")
    void deleteOldVerifiedOtps(@Param("time") LocalDateTime time);

    @Modifying
    @Transactional
    @Query("""
      UPDATE OtpVerification o
      SET o.verified = true
      WHERE o.email = :email
        AND o.purpose = :purpose
        AND o.verified = false
        """)
        void invalidatePreviousOtps(String email, OtpPurpose purpose);
}
