package com.smartims.service;

import com.smartims.enums.OtpPurpose;

public interface EmailService {
    void sendOtpEmail(String email, String otp, OtpPurpose purpose);
    void sendContactAcknowledgement(String email, String name);
}
