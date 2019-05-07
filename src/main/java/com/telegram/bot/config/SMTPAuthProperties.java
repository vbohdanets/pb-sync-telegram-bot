package com.telegram.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties("spring.mail")
@Validated
public class SMTPAuthProperties {

    @NotEmpty(message = "spring.mail.username property must not be empty")
    private String username;

    @NotEmpty(message = "spring.mail.password property must not be empty")
    private String password;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
