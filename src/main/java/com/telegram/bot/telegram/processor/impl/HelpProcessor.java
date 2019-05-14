package com.telegram.bot.telegram.processor.impl;

import com.telegram.bot.telegram.processor.CommandProcessor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.telegram.bot.telegram.constants.Constants.HELP_COMMAND;

public class HelpProcessor implements CommandProcessor {
    @Override
    public void process(Message payload, SendMessage sendMessage) {
        sendMessage.setText("/ping - very usefull thing\n" +
                "/setup_email - setting up an pbsync email\n" +
                "THAT'S ALL, FOLKS!");
    }

    @Override
    public String getCommand() {
        return HELP_COMMAND;
    }
}
