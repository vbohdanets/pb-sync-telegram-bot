package com.telegram.bot.telegram.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bot.telegram.ChatIdEmailsDao;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public class ChatIdEmailsMapDaoImpl implements ChatIdEmailsDao {

    private static final Logger logger = LoggerFactory.getLogger(ChatIdEmailsMapDaoImpl.class);
    private static final String fileName = "serializedMap.json";
    private Map<String, String> chatIdEmailsMap = new HashMap<>();
    private final  ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        File serializedJSONMap = new File(fileName);
        if (serializedJSONMap.exists()) {
            try {
                chatIdEmailsMap.putAll(mapper.readValue(serializedJSONMap, new TypeReference<Map<String, String>>(){}));
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
        chatIdEmailsMap.put(chatId.toString(), email);
    }

    @Override
    public String getEmail(Long chatId) {
        return chatIdEmailsMap.get(chatId.toString());
    }

    @Override
    public void deleteEmail(Long chatId) {
        chatIdEmailsMap.remove(chatId.toString());
    }


}
