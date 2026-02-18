package dev.lepelaka.kiosk.domain.order.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    TERMINAL_NOT_FOUND("ORDER-001", "터이널을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND("ORDER-002", "상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND("ORDER-003", "주문을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    INSUFFICIENT_STOCK("ORDER-101", "재고가 부족합니다", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY("ORDER-102", "유효하지 않은 수량입니다", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS("ORDER-103", "유효하지 않은 주문상태입니다", HttpStatus.BAD_REQUEST),
    EMPTY_ORDER_ITEMS("ORDER-104", "주문 항목이 비어있습니다", HttpStatus.BAD_REQUEST),
    INACTIVE_PRODUCT_IN_ORDER("ORDER-105", "비활성 상품이 포함되어있습니다", HttpStatus.BAD_REQUEST),

    ORDER_ALREADY_CONFIRMED("ORDER-201", "이미 확정된 주문입니다", HttpStatus.CONFLICT),
    ORDER_ALREADY_CANCELED("ORDER-202", "이미 취소된 주문입니다", HttpStatus.CONFLICT),
    CANNOT_CANCEL_ORDER("ORDER-203", "취소할 수 없는 주문입니다", HttpStatus.FORBIDDEN),

    ORDER_CREATION_FAILED("ORDER-901", "주문 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_NUMBER_GENERATION_FAILED("ORDER-902", "주문번호 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
