package com.telegram.bot.telegram;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Component
public class PBSyncBot extends TelegramLongPollingBot {

    private static final String telegramUrl = "https://api.telegram.org/file/bot";

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.username}")
    private String username;

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
            try {
                saveToFile(document.getFileName(), document.getFileId());
            } catch (IOException | TelegramApiException e) {
                //TODO add proper handler
                e.printStackTrace();
            }
        }

    }

    private void saveToFile(String filename, String fieldId) throws IOException, TelegramApiException {
        //TODO hold stream in memory for sending email, then clear. No need to save file(perhaps...)
        java.io.File localFile = new java.io.File("~/Documents/" + filename);
        InputStream is = new URL(telegramUrl + token + "/" + getFilePath(fieldId)).openStream();
        FileUtils.copyInputStreamToFile(is,localFile);
    }

    private String getFilePath(String fieldId) throws TelegramApiException {
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(fieldId);
        return getFile(uploadedFile).getFilePath();
    }

    private File getFile(GetFile uploadedFile) throws TelegramApiException {
        return execute(uploadedFile);
    }
}
