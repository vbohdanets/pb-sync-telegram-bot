package com.telegram.bot.telegram.dao;

import com.telegram.bot.telegram.model.TelegramMetadata;

public interface JSONStorageDao {
    void saveEmail(Long chatId, String email);
    void setCommand(Long chatId, String command);
    void delete(Long chatId);
    TelegramMetadata readByChatId(Long chatId);
}