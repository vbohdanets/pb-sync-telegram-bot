package com.telegram.bot.telegram;

import com.telegram.bot.emailsender.EmailSender;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Component
public class PBSyncBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(PBSyncBot.class);
    private static final String telegramUrl = "https://api.telegram.org/file/bot";

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.username}")
    private String username;

    private final EmailSender emailSender;

    public PBSyncBot(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasDocument()) {
            Document document = update.getMessage().getDocument();
            Long chatId = update.getMessage().getChatId();
            try(InputStream fileInputStream = getFileInputStream(document.getFileId())) {

                sendUploadDocumentAction(chatId);
                emailSender.sendMessageWithAttachment("jicawar@business-agent.info", "KEK", document.getFileName(), fileInputStream, document.getMimeType());
                sendSuccessMessage(chatId);
            } catch (IOException e) {
                logger.error("Error while IO operation, {}", e.getMessage(), e);
                sendFailMessage(chatId, "Developer made a huuuuuuge oopsie! Try again a bit later!");
            } catch (MessagingException e) {
                logger.error("Error while sending email, {}", e.getMessage(), e);
                sendFailMessage(chatId, "Developer made an oopsie, so I can't sent an email =( ! Try again a bit later!");
            } catch (TelegramApiException e) {
                logger.error("Error while sending message, {}", e.getMessage(), e);
            }
        }
    }

    private void sendFailMessage(Long chatId, String messageText) {
       try {
           SendMessage message = new SendMessage();
           message.setChatId(chatId);
           message.setText(messageText);
           execute(message);
       } catch (TelegramApiException e) {
           logger.error("Error while sending message, {}", e.getMessage(), e);
       }
    }

    private void sendSuccessMessage(Long chatId){
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("File was successfully sent\uD83C\uDF1A");
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error while sending message, {}", e.getMessage(), e);
        }
    }

    private void sendUploadDocumentAction(Long chatId) {
        try {
            SendChatAction sendChatAction = new SendChatAction();
            sendChatAction.setChatId(chatId);
            sendChatAction.setAction(ActionType.UPLOADDOCUMENT);
            execute(sendChatAction);
        } catch (TelegramApiException e) {
            logger.error("Error while sending message, {}", e.getMessage(), e);
        }
    }

    private InputStream getFileInputStream(String fieldId) throws IOException, TelegramApiException {
        return new URL(telegramUrl + token + "/" + getFilePath(fieldId)).openStream();
    }

    private String getFilePath(String fieldId) throws TelegramApiException {
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(fieldId);
        return getUploadedFile(uploadedFile).getFilePath();
    }

    private File getUploadedFile(GetFile uploadedFile) throws TelegramApiException {
        return execute(uploadedFile);
    }
}
