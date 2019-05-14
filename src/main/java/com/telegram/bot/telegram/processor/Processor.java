package com.telegram.bot.telegram.processor;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface Processor {
    void process(Message payload, SendMessage sendMessage);
}
