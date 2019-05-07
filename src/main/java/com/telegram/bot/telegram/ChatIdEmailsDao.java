package com.telegram.bot.telegram;

public interface ChatIdEmailsDao {

    void saveEmail(Long chatId, String email);
    String getEmail(Long chatId);
    void deleteEmail(Long chatId);
}
