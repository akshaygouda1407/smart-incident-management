package com.smartims.service.impl;

import com.smartims.enums.OtpPurpose;
import com.smartims.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

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
    }

    private void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
