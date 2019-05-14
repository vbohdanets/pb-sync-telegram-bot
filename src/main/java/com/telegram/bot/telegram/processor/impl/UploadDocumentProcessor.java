package com.telegram.bot.telegram.processor.impl;

import com.telegram.bot.emailsender.EmailSender;
import com.telegram.bot.telegram.PBSyncBot;
import com.telegram.bot.telegram.dao.JSONStorageDao;
import com.telegram.bot.telegram.processor.DocumentProcessor;
import com.telegram.bot.telegram.processor.Processor;
import com.telegram.bot.telegram.validator.FormatValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class UploadDocumentProcessor implements DocumentProcessor {
    private static final Logger logger = LoggerFactory.getLogger(UploadDocumentProcessor.class);

    private final FormatValidator validator;
    private final EmailSender emailSender;
    private final JSONStorageDao jsonStorageDao;

    private InputStream inputStream;

    public UploadDocumentProcessor(FormatValidator validator, EmailSender emailSender, JSONStorageDao jsonStorageDao) {
        this.validator = validator;
        this.emailSender = emailSender;
        this.jsonStorageDao = jsonStorageDao;
    }

    @Override
    public void process(Message payload, SendMessage sendMessage) {
        Document document = payload.getDocument();
        Long chatId = payload.getChatId();

        if (!validator.isFormatValid(document.getFileName())) {
            sendMessage.setText("Sorry, I can't handle that format =(");
            return;
        }

        String email = jsonStorageDao.readByChatId(chatId).getEmail();
        if (email == null) {
            sendMessage.setText("Please enter your pocket book email, so I could sent books to you\nExample: /setup_email pbsync@pbsync.com");
        }
        try (InputStream fileInputStream = inputStream) {
            emailSender.sendMessageWithAttachment(email, "PocketBook sync telegram bot", document.getFileName(), fileInputStream, document.getMimeType());
            sendMessage.setText("File was successfully sent\uD83C\uDF1A");
        } catch (IOException e) {
            logger.error("Error while IO operation, {}", e.getMessage(), e);
            sendMessage.setText("Developer made a huuuuuuge oopsie! Try again a bit later!");
        } catch (MessagingException e) {
            logger.error("Error while sending email, {}", e.getMessage(), e);
            sendMessage.setText("Developer made an oopsie, so I can't sent an email =( ! Try again a bit later!");
        }
    }

    @Override
    public Processor setDocumentInputStream(InputStream inputStream) {
       this.inputStream = inputStream;
       return this;
    }
}
