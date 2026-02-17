package com.smartims.service;

import com.smartims.enums.OtpPurpose;

public interface EmailService {
    void sendOtpEmail(String email, String otp, OtpPurpose purpose);
    void sendContactAcknowledgement(String email, String name);
    void sendNewUserCredentialsEmail(String email, String fullName, String username, String rawPassword, String loginUrl);
}
