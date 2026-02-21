package dev.lepelaka.kiosk.domain.category.exception;

import dev.lepelaka.kiosk.domain.order.exception.InsufficientStockException;
import dev.lepelaka.kiosk.global.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "dev.lepelaka.kiosk.domain.category")
public class CategoryExceptionHandler {
    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<ErrorResponse> handleCategoryException(CategoryException ex) {
        log.warn("Category exception : {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(ex.getErrorCode());

        return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
    }
}
