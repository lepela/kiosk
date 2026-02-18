package dev.lepelaka.kiosk.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;
    private final Map<String, Object> details;

    public static ErrorResponse of(ErrorCode code) {
        return ErrorResponse.builder()
                .code(code.getCode())
                .message(code.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(ErrorCode code, Map<String, Object> details) {
        return ErrorResponse.builder()
                .code(code.getCode())
                .message(code.getMessage())
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }
}
