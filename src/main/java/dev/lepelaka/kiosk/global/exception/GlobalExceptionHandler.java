package dev.lepelaka.kiosk.global.exception;

import dev.lepelaka.kiosk.domain.category.exception.CategoryException;
import dev.lepelaka.kiosk.domain.order.exception.OrderException;
import dev.lepelaka.kiosk.domain.product.exception.ProductException;
import dev.lepelaka.kiosk.domain.terminal.exception.TerminalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorResponse> handleProductException(ProductException ex) {
        log.warn("Product exception [{}] : {}", ex.getErrorCode().getCode(), ex.getMessage());
        return buildResponse(ex);
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderException ex) {
        log.warn("Order exception [{}] : {}", ex.getErrorCode().getCode(), ex.getMessage());
        return buildResponse(ex);
    }

    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<ErrorResponse> handleCategoryException(CategoryException ex) {
        log.warn("Category exception [{}] : {}", ex.getErrorCode().getCode(), ex.getMessage());
        return buildResponse(ex);
    }

    @ExceptionHandler(TerminalException.class)
    public ResponseEntity<ErrorResponse> handleTerminalException(TerminalException ex) {
        log.warn("Terminal exception [{}] : {}", ex.getErrorCode().getCode(), ex.getMessage());
        return buildResponse(ex);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business exception [{}] : {}", ex.getErrorCode().getCode(), ex.getMessage());
        return buildResponse(ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
                ));

        List<String> globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        Map<String, Object> errors = new HashMap<>(fieldErrors);
        if (!globalErrors.isEmpty()) {
            errors.put("_global", globalErrors);
        }
        ErrorResponse response = ErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message("입력값 검증에 실패했습니다.")
                .details(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorResponse response = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 오류가 발생했습니다.")
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(BusinessException ex) {
        Map<String, Object> details = ex.getDetails();
        ErrorResponse response = details.isEmpty()
                ? ErrorResponse.of(ex.getErrorCode())
                : ErrorResponse.of(ex.getErrorCode(), details);
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }
}
