package com.smartims.service.impl;

import com.smartims.entity.ContactMessage;
import com.smartims.repository.ContactRepository;
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

    @Override
    public void submit(ContactMessage message) {
        message.setCreatedAt(LocalDateTime.now());
        contactRepository.save(message);
        emailService.sendContactAcknowledgement(
                message.getEmail(),
                message.getName()
        );
    }
}
