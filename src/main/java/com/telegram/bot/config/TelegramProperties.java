package com.telegram.bot.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;


@ConfigurationProperties("telegram")
@Validated
public class TelegramProperties {

    /**
     * Telegram API token generated from BotFather
     */
    @NotEmpty(message = "Token must not be empty")
    private String token = "";

    /**
     * BotName that you've entered during setting up bot
     */
    @NotEmpty(message = "Botname must not be empty")
    private String botname = "";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBotname() {
        return botname;
    }

    public void setBotname(String botname) {
        this.botname = botname;
    }
}
