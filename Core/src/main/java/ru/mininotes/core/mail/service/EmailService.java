package ru.mininotes.core.mail.service;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import ru.mininotes.core.mail.service.dto.AbstractEmailContext;

import java.io.FileNotFoundException;

@Service
public interface EmailService {

//    void sendTextEmail();
//
//    void sendEmailWithAttachment();
//
//    void sendHTMLEmail();
//    void sendMail(AbstractEmailContext email) throws MessagingException;

    void sendSimpleEmail(String toAddress, String subject, String message);

    void sendEmailWithAttachment(String toAddress, String subject, String message, String attachment) throws MessagingException, FileNotFoundException;
}
