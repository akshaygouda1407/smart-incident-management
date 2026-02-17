package com.smartims.service.impl;

import com.smartims.enums.OtpPurpose;
import com.smartims.service.AuditLogService;
import com.smartims.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final AuditLogService auditLogService;

    @Value("${app.mail.from:}")
    private String fromAddress;

    @Override
    public void sendOtpEmail(String email, String otp, OtpPurpose purpose) {

        String subject = purpose == OtpPurpose.REGISTER
                ? "Verify Your ServicePlus Account"
                : "Reset Your ServicePlus Password";

        String body = """
                Hello,

                Your OTP is: %s

                This OTP is valid for 3 minutes.
                Do not share it with anyone.

                – ServicePlus Team
                """.formatted(otp);

        sendMail(email, subject, body);

        auditLogService.logSystem(
                "OTP_EMAIL_SENT",
                "OTP email sent for purpose: " + purpose,
                null,
                "EMAIL"
        );
    }

    @Override
    public void sendContactAcknowledgement(String email, String name) {

        String subject = "We Received Your Message – ServicePlus";

        String body = """
                Hello %s,

                Thank you for contacting ServicePlus.
                We have received your message and our team will respond shortly.

                Regards,
                ServicePlus Support Team
                """.formatted(name);

        sendMail(email, subject, body);

        auditLogService.logSystem(
                "CONTACT_ACK_EMAIL_SENT",
                "Contact acknowledgement email sent to " + email,
                null,
                "EMAIL"
        );
    }

    @Override
    public void sendNewUserCredentialsEmail(
            String email,
            String fullName,
            String username,
            String rawPassword,
            String loginUrl
    ) {
        String subject = "Your ServicePlus Account Details";

        String safeName = (fullName == null || fullName.isBlank()) ? "Hello" : "Hello " + fullName;

        String body = """
                %s,

                Your account has been created.

                Username: %s
                Password: %s

                Login here: %s

                For security, please change your password after logging in.

                – ServicePlus Team
                """.formatted(safeName, username, rawPassword, loginUrl);

        sendMail(email, subject, body);

        auditLogService.logSystem(
                "NEW_USER_CREDENTIALS_EMAIL_SENT",
                "New user credentials email sent to " + email,
                null,
                "EMAIL"
        );
    }

    private void sendMail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
