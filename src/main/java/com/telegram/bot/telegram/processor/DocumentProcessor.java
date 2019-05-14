package com.telegram.bot.telegram.processor;

import java.io.InputStream;
import java.util.function.Function;

public interface DocumentProcessor extends Processor {

    Processor setDocumentInputStream(InputStream fieldId);
}
