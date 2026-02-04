package com.smartims.service.impl;

import com.smartims.entity.ContactMessage;
import com.smartims.repository.ContactRepository;
import com.smartims.service.AuditLogService;
import com.smartims.service.ContactService;
import com.smartims.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Override
    public void submit(ContactMessage message) {

        message.setCreatedAt(LocalDateTime.now());
        ContactMessage savedMessage = contactRepository.save(message);

        emailService.sendContactAcknowledgement(
                message.getEmail(),
                message.getName()
        );

        auditLogService.log(
                "CONTACT_MESSAGE_SUBMITTED",
                "CONTACT_MESSAGE",
                savedMessage.getId(),
                "Contact message submitted by "
                        + message.getName()
                        + " with email "
                        + message.getEmail()
        );
    }
}
