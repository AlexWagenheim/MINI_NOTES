package ru.mininotes.core.mail.service.implementation;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.mininotes.core.mail.service.EmailService;
import ru.mininotes.core.mail.service.dto.AbstractEmailContext;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

@Service
public class DefaultEmailService implements EmailService {

//    @Autowired
//    private JavaMailSender emailSender;
//
//    @Autowired
//    private SpringTemplateEngine templateEngine;
//
//    @Override
//    public void sendMail(AbstractEmailContext email) throws MessagingException {
//        MimeMessage message = emailSender.createMimeMessage();
//        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message,
//                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
//                StandardCharsets.UTF_8.name());
//        Context context = new Context();
//        context.setVariables(email.getContext());
//        String emailContent = templateEngine.process(email.getTemplateLocation(), context);
//
//        mimeMessageHelper.setTo(email.getTo());
//        mimeMessageHelper.setSubject(email.getSubject());
//        mimeMessageHelper.setFrom(email.getFrom());
//        mimeMessageHelper.setText(emailContent, true);
//        emailSender.send(message);
//    }

    @Autowired
    public JavaMailSender emailSender;

    @Override
    public void sendSimpleEmail(String toAddress, String subject, String message) {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(toAddress);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        emailSender.send(simpleMailMessage);
    }

    @Override
    public void sendEmailWithAttachment(String toAddress, String subject, String message, String attachment) throws MessagingException, FileNotFoundException {

        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
        messageHelper.setTo(toAddress);
        messageHelper.setSubject(subject);
        messageHelper.setText(message);
        FileSystemResource file = new FileSystemResource(ResourceUtils.getFile(attachment));
        messageHelper.addAttachment("Purchase Order", file);
        emailSender.send(mimeMessage);
    }
}
