package com.example.backend.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    public static <T> CommonApiResponse<T> createSuccess(T data) {
        return new CommonApiResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data);
    }

    public static <T> CommonApiResponse<T> createSuccess(String message, T data) {
        return new CommonApiResponse<>(true, "SUCCESS", message, data);
    }

    public static <T> CommonApiResponse<T> createFailure(String code, String message) {
        return new CommonApiResponse<>(false, code, message, null);
    }
}
