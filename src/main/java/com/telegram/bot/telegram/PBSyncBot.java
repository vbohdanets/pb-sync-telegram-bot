package com.telegram.bot.telegram;

import com.telegram.bot.config.TelegramProperties;
import com.telegram.bot.telegram.processor.CommandProcessor;
import com.telegram.bot.telegram.processor.DocumentProcessor;
import com.telegram.bot.telegram.processor.Processor;
import com.telegram.bot.telegram.processor.impl.HelpProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EnableConfigurationProperties(TelegramProperties.class)
@Component
public class PBSyncBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(PBSyncBot.class);
    private static final String TELEGRAM_URL = "https://api.telegram.org/file/bot";

    private final TelegramProperties properties;
    private final List<Processor> commandProcessors;

    private Map<String, CommandProcessor> commandProcessorMap;
    private DocumentProcessor documentProcessor;

    public PBSyncBot(TelegramProperties properties, List<Processor> processors, DocumentProcessor documentProcessor) {
        this.properties = properties;
        this.commandProcessors = processors;
        this.documentProcessor = documentProcessor;
        commandProcessorMap = processors.stream()
                .filter(i -> (i instanceof CommandProcessor))
                .map(i -> (CommandProcessor) i)
                .collect(Collectors.toMap(CommandProcessor::getCommand, v -> v));
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
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            Message message = update.getMessage();
            try {
                if (message.hasText() && message.getText().startsWith("/")) {
                    commandProcessorMap
                            .getOrDefault(message.getText(), new HelpProcessor())
                            .process(message, sendMessage);
                }

                if (message.hasDocument()) {
                    sendUploadDocumentAction(message.getChatId());
                    documentProcessor
                            .setDocumentInputStream(getFileInputStream(message.getDocument().getFileId()))
                            .process(message, sendMessage);

                }
                execute(sendMessage);
            } catch (IOException e) {
                logger.error("Error while IO operation, {}", e.getMessage(), e);
                sendMessage.setText("Developer made a huuuuuuge oopsie! Try again a bit later!");
                execute(sendMessage);
            } catch (TelegramApiException e) {
                logger.error("Error while sending message, {}", e.getMessage(), e);
            }
        }
    }

    private void execute(SendMessage sendMessage) {
        try {
            super.execute(sendMessage);
        } catch (TelegramApiException ignored) {}
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
    private String getFilePath(String fieldId) throws TelegramApiException {
        GetFile uploadedFile = new GetFile();
        uploadedFile.setFileId(fieldId);
        return execute(uploadedFile).getFilePath();
    }

    private InputStream getFileInputStream(String fieldId) throws IOException, TelegramApiException {
        return new URL(TELEGRAM_URL + properties.getToken() + "/" + getFilePath(fieldId)).openStream();
    }
}
