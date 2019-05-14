package com.telegram.bot.telegram.processor.impl;

import com.telegram.bot.telegram.processor.CommandProcessor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Random;

import static com.telegram.bot.telegram.constants.Constants.PING_COMMAND;

@Component
public class PingCommandProcessor implements CommandProcessor {

    private List<String> pingCommands = List.of("HODOR", "kept you waiting huh", "Nani?", "Java allows it!", "Arsene who?", "who's there?");
    @Override
    public String getCommand() {
        return PING_COMMAND;
    }

    @Override
    public void process(Message payload, SendMessage sendMessage) {
        String message = pingCommands.get(new Random().nextInt(pingCommands.size()));
        sendMessage.setText(message);
    }
}
