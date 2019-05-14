package com.telegram.bot.telegram.model;

public class TelegramMetadata {

    public TelegramMetadata(String email, String command) {
        this.email = email;
        this.command = command;
    }

    public TelegramMetadata() {
    }

    private String email;
    private String command;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
