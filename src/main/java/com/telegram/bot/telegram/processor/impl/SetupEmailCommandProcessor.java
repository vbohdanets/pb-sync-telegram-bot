package com.telegram.bot.telegram.processor.impl;

import com.telegram.bot.telegram.dao.JSONStorageDao;
import com.telegram.bot.telegram.processor.CommandProcessor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.telegram.bot.telegram.constants.Constants.SETUP_EMAIL_COMMAND;

@Component
public class SetupEmailCommandProcessor implements CommandProcessor {

    private final JSONStorageDao jsonStorageDao;

    public SetupEmailCommandProcessor(JSONStorageDao jsonStorageDao) {
        this.jsonStorageDao = jsonStorageDao;
    }

    @Override
    public String getCommand() {
        return SETUP_EMAIL_COMMAND;
    }

    @Override
    public void process(Message payload, SendMessage sendMessage) {
        String userMessage = payload.getText();
        String userEmail = userMessage.replace(SETUP_EMAIL_COMMAND, "").strip();
        if (userEmail.isBlank()) {
            sendMessage.setText("Please enter an email");
            return;
        }
        jsonStorageDao.saveEmail(payload.getChatId(), userEmail);
        sendMessage.setText("Email was successfully saved!");
    }
}
