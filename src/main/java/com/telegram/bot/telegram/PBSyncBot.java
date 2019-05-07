package com.telegram.bot.telegram;

import com.telegram.bot.config.TelegramProperties;
import com.telegram.bot.emailsender.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

@EnableConfigurationProperties(TelegramProperties.class)
@Component
public class PBSyncBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(PBSyncBot.class);
    private static final String telegramUrl = "https://api.telegram.org/file/bot";
    private static final String setupEmailCommand = "/setup_email";

    private final EmailSender emailSender;
    private final TelegramProperties properties;
    private final ChatIdEmailsDao chatIdEmailsDao;

    public PBSyncBot(EmailSender emailSender, ChatIdEmailsDao chatIdEmailsDao, TelegramProperties properties) {
        this.emailSender = emailSender;
        this.properties = properties;
        this.chatIdEmailsDao = chatIdEmailsDao;
    }

    @Override
    public String getBotUsername() {
        return properties.getBotname();
    }

    @Override
    public String getBotToken() {
        return properties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            try {
                String userMessage = update.getMessage().getText();
                if (userMessage != null && userMessage.isBlank() && userMessage.startsWith(setupEmailCommand)) {
                    saveUserPocketBookSyncEmail(chatId, userMessage);
                }

                if (update.getMessage().hasDocument()) {
                    Document document = update.getMessage().getDocument();
                    handleDocument(document, chatId);

                }
            } catch (TelegramApiException e) {
                logger.error("Error while sending message, {}", e.getMessage(), e);
            }
        }
    }

    private void handleDocument(Document document, Long chatId) throws TelegramApiException {
        String email = chatIdEmailsDao.getEmail(chatId);
        if (email == null) {
            showSavingEmailAwareMessage(chatId);
        }
        try (InputStream fileInputStream = getFileInputStream(document.getFileId())) {
            sendUploadDocumentAction(chatId);
            emailSender.sendMessageWithAttachment(email, "PocketBook sync telegram bot", document.getFileName(), fileInputStream, document.getMimeType());
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

    private void showSavingEmailAwareMessage(Long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Please enter your pocket book email, so I could sent books to you\nExample: /setup_email pbsync@pbsync.com");
        execute(message);
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

    private void sendSuccessMessage(Long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error while sending message, {}", e.getMessage(), e);
        }
    }

    private void sendSuccessMessage(Long chatId) {
        sendSuccessMessage(chatId, "File was successfully sent\uD83C\uDF1A");
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

    private void saveUserPocketBookSyncEmail(Long chatId, String userMessage) {
        String userEmail = userMessage.replace(setupEmailCommand, "").strip();
        chatIdEmailsDao.saveEmail(chatId, userEmail);
        sendSuccessMessage(chatId, "Email was successfully saved!");
    }

    private String getFilePath(String fieldId) throws TelegramApiException {
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(fieldId);
        return getUploadedFile(uploadedFile).getFilePath();
    }

    private InputStream getFileInputStream(String fieldId) throws IOException, TelegramApiException {
        return new URL(telegramUrl + properties.getToken() + "/" + getFilePath(fieldId)).openStream();
    }

    private File getUploadedFile(GetFile uploadedFile) throws TelegramApiException {
        return execute(uploadedFile);
    }
}
