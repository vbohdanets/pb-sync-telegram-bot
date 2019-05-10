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
    private static final String TELEGRAM_URL = "https://api.telegram.org/file/bot";
    private static final String SETUP_EMAIL_COMMAND = "/setup_email";
    private static final String PING_MESSAGE = "hodor";

    private final EmailSender emailSender;
    private final TelegramProperties properties;
    private final ChatIdEmailsDao chatIdEmailsDao;
    private final FormatValidator validator;

    public PBSyncBot(EmailSender emailSender, ChatIdEmailsDao chatIdEmailsDao, TelegramProperties properties, FormatValidator validator) {
        this.emailSender = emailSender;
        this.properties = properties;
        this.chatIdEmailsDao = chatIdEmailsDao;
        this.validator = validator;
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
                if (userMessage != null && !userMessage.isBlank()) {
                    processUserText(chatId, userMessage);
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

    private void processUserText(Long chatId, String userMessage) {
        if (userMessage.startsWith(SETUP_EMAIL_COMMAND)) {
            saveUserPocketBookSyncEmail(chatId, userMessage);
        }
        if (userMessage.toLowerCase().contains(PING_MESSAGE)) {
            sendMessage(chatId, PING_MESSAGE);
        }
    }

    private void handleDocument(Document document, Long chatId) throws TelegramApiException {
        if (!validator.isFormatValid(document.getFileName())) {
            sendMessage(chatId, "Sorry, I can't handle that format =(");
            return;
        }

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
            sendMessage(chatId, "Developer made a huuuuuuge oopsie! Try again a bit later!");
        } catch (MessagingException e) {
            logger.error("Error while sending email, {}", e.getMessage(), e);
            sendMessage(chatId, "Developer made an oopsie, so I can't sent an email =( ! Try again a bit later!");
        }
    }

    private void sendMessage(Long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
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

    private void showSavingEmailAwareMessage(Long chatId) {
        sendMessage(chatId, "Please enter your pocket book email, so I could sent books to you\nExample: /setup_email pbsync@pbsync.com");
    }

    private void sendSuccessMessage(Long chatId) {
        sendMessage(chatId, "File was successfully sent\uD83C\uDF1A");
    }

    private void saveUserPocketBookSyncEmail(Long chatId, String userMessage) {
        String userEmail = userMessage.replace(SETUP_EMAIL_COMMAND, "").strip();
        if (userEmail.isBlank()) {
            sendMessage(chatId, "Please enter an email");
            return;
        }
        chatIdEmailsDao.saveEmail(chatId, userEmail);
        sendMessage(chatId, "Email was successfully saved!");
    }

    private String getFilePath(String fieldId) throws TelegramApiException {
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(fieldId);
        return execute(uploadedFile).getFilePath();
    }

    private InputStream getFileInputStream(String fieldId) throws IOException, TelegramApiException {
        return new URL(TELEGRAM_URL + properties.getToken() + "/" + getFilePath(fieldId)).openStream();
    }
}
