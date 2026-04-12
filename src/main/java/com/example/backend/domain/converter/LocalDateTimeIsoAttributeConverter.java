package com.example.backend.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * SQLite 등에서 JDBC 타임스탬프 파싱 오류를 피하기 위해 {@link LocalDateTime}을 문자열로 저장합니다.
 */
@Converter(autoApply = false)
public class LocalDateTimeIsoAttributeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter WRITE = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return WRITE.format(attribute);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        String s = dbData.trim();
        try {
            return LocalDateTime.parse(s, WRITE);
        } catch (DateTimeParseException ignored) {
            // Hibernate/SQLite가 'yyyy-MM-dd HH:mm:ss' 형태로 넣은 기존 행 호환
        }
        if (s.length() >= 10 && s.charAt(10) == ' ') {
            String normalized = s.substring(0, 10) + 'T' + s.substring(11);
            try {
                return LocalDateTime.parse(normalized, WRITE);
            } catch (DateTimeParseException ignored) {
                // fall through
            }
        }
        for (String pattern : new String[]{
                "yyyy-MM-dd HH:mm:ss.SSSSSSSSS",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss"
        }) {
            try {
                return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        throw new IllegalArgumentException("LocalDateTime으로 변환할 수 없습니다: " + dbData);
    }
}
