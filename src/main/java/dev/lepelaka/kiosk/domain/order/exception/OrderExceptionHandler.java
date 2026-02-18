package dev.lepelaka.kiosk.domain.order.exception;

import dev.lepelaka.kiosk.global.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "dev.lepelaka.kiosk.domain.order")
public class OrderExceptionHandler {
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderException ex) {
        log.warn("Order exception : {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(ex.getErrorCode());

        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException ex) {
        log.warn("Insufficient stock : {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(ex.getErrorCode(), ex.getDetails());

        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }
}
