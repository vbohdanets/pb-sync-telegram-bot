package com.telegram.bot.emailsender;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface EmailSender {
    void sendMessageWithAttachment(String to, String subject, String fileName, InputStream inputStream, String contentType) throws MessagingException, IOException;
}
