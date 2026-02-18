package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "dev.lepelaka.kiosk.domain.product")
public class ProductExceptionHandler {

    @ExceptionHandler(ProdcutException.class)
    public ResponseEntity<ErrorResponse> handleException(ProdcutException e) {
        log.warn("Product Exception : {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(ProductNotFoundException e) {
        log.warn("Product Not Found Exception : {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(response);
    }
}
