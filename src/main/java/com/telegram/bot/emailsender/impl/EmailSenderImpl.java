package com.telegram.bot.emailsender.impl;

import com.telegram.bot.emailsender.EmailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;

@Component
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender javaMailSender;

    public EmailSenderImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendMessageWithAttachment(String to, String subject, String fileName, InputStream inputStream, String contentType) throws MessagingException, IOException {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED);

        helper.setFrom("PockerBook_Sender_bot");
        helper.setTo(to);
        helper.setSubject(subject);

        helper.addAttachment(fileName, new ByteArrayDataSource(inputStream, contentType));

        javaMailSender.send(message);
    }
}
