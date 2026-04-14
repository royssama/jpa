package com.example.backend.api;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CommonApiResponseFactory {

    private final MessageSource messageSource;

    public CommonApiResponseFactory(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public <T> CommonApiResponse<T> success(String messageKey, T data, Locale locale) {
        String message = messageSource.getMessage(messageKey, null, messageKey, locale);
        return new CommonApiResponse<>(true, "SUCCESS", message, data);
    }

    public <T> CommonApiResponse<T> success(T data, Locale locale) {
        return success("response.success", data, locale);
    }

    public <T> CommonApiResponse<T> failure(String code, String messageKey, Locale locale) {
        String message = messageSource.getMessage(messageKey, null, messageKey, locale);
        return new CommonApiResponse<>(false, code, message, null);
    }
}
