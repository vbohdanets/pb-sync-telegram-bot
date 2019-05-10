package com.telegram.bot.telegram.impl;

import com.telegram.bot.telegram.FormatValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormatValidatorImpl implements FormatValidator {
    private static final List<String> VALID_FORMATS = List.of("epub", "pdf", "fb2", "txt", "djvu", "htm", "html", "doc", "docx", "rtf");
    @Override
    public boolean isFormatValid(String filename) {
        return VALID_FORMATS.contains(filename.substring(filename.lastIndexOf(".") + 1));
    }
}
