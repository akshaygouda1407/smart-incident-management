package com.smartims.service;

import com.smartims.enums.OtpPurpose;

public interface OtpService {
    void generateAndSendOtp(String email, OtpPurpose purpose);
    void verifyOtp(String email, String otp, OtpPurpose purpose);
    boolean isOtpVerified(String email, OtpPurpose purpose);

}

