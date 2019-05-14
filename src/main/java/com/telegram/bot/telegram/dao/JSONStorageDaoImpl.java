package com.telegram.bot.telegram.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bot.telegram.model.TelegramMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JSONStorageDaoImpl implements JSONStorageDao {

    private static final Logger logger = LoggerFactory.getLogger(JSONStorageDaoImpl.class);
    private static final String fileName = "serializedMap.json";
    private Map<String, TelegramMetadata> chatIdEmailsMap = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        File serializedJSONMap = new File(fileName);
        if (serializedJSONMap.exists()) {
            try {
                chatIdEmailsMap.putAll(mapper.readValue(serializedJSONMap, new TypeReference<Map<String, TelegramMetadata>>() {}));
            } catch (IOException e) {
                logger.error("Cannot deserialize object, {}", e.getMessage(), e);
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (chatIdEmailsMap.isEmpty()) {
            return;
        }
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(fileName), chatIdEmailsMap);
        } catch (IOException e) {
            logger.error("Cannot read serialize object, {}", e.getMessage(), e);
        }
    }

    @Override
    public void saveEmail(Long chatId, String email) {
        TelegramMetadata metadata;
        String chatIdString = chatId.toString();
        if (chatIdEmailsMap.containsKey(chatIdString)) {
            metadata = chatIdEmailsMap.get(chatIdString);
        } else {
            metadata = new TelegramMetadata(email, null);
        }
        chatIdEmailsMap.put(chatIdString, metadata);
    }

    @Override
    public void setCommand(Long chatId, String command) {
        String chatIdString = chatId.toString();
        TelegramMetadata metadata = chatIdEmailsMap.get(chatIdString);
        metadata.setCommand(command);
        chatIdEmailsMap.put(chatIdString, metadata);
    }

    @Override
    public void delete(Long chatId) {
        chatIdEmailsMap.remove(chatId.toString());
    }

    @Override
    public TelegramMetadata readByChatId(Long chatId) {
        return chatIdEmailsMap.get(chatId.toString());
    }
}
