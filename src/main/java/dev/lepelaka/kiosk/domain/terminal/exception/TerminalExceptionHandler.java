package dev.lepelaka.kiosk.domain.terminal.exception;

import dev.lepelaka.kiosk.global.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "dev.lepelaka.kiosk.domain.terminal")
public class TerminalExceptionHandler {
    @ExceptionHandler(TerminalException.class)
    public ResponseEntity<ErrorResponse> handleTerminalException(TerminalException ex) {
        log.warn("Terminal exception : {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }
}
