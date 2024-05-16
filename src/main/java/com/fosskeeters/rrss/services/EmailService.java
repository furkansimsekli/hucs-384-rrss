package com.fosskeeters.rrss.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender emailSender;
    private final String fromAddress;

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
        this.fromAddress = "info.fosskeeters@gmail.com";
    }

    /**
     * Sends an email with the specified recipient, subject, and content asynchronously. This method
     * will be executed in a separate thread, allowing the calling thread to continue processing
     * without being blocked.
     *
     * @param to      The email address of the recipient
     * @param subject The subject of the email
     * @param content The content of the email
     * @throws MessagingException If any messaging error occurs during the sending process
     */
    @Async
    public void send(String to, String subject, String content) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, /*multipart=*/true);
        helper.setFrom(this.fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, /*html=*/true);
        emailSender.send(message);
    }
}