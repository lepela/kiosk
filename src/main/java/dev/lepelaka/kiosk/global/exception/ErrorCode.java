package dev.lepelaka.kiosk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Product ──────────────────────────────────────────────────────────────
    PRODUCT_NOT_FOUND("PRODUCT-001", "상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    INVALID_PRODUCT_NAME("PRODUCT-101", "유효하지 않은 상품명입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PRICE("PRODUCT-102", "유효하지 않은 가격입니다", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY("PRODUCT-103", "유효하지 않은 수량입니다", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY("PRODUCT-104", "유효하지 않은 카테고리입니다", HttpStatus.BAD_REQUEST),

    INACTIVE_PRODUCT("PRODUCT-201", "비활성 상태의 상품입니다.", HttpStatus.FORBIDDEN),
    DUPLICATE_PRODUCT_NAME("PRODUCT-202", "이미 존재하는 상품명입니다", HttpStatus.CONFLICT),
    CANNOT_DELETE_PRODUCT("PRODUCT-203", "삭제할 수 없는 상품입니다.", HttpStatus.FORBIDDEN),

    PRODUCT_CREATION_FAILED("PRODUCT-901", "상품 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    // ── Order ────────────────────────────────────────────────────────────────
    ORDER_TERMINAL_NOT_FOUND("ORDER-001", "터미널을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ORDER_PRODUCT_NOT_FOUND("ORDER-002", "상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND("ORDER-003", "주문을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    INSUFFICIENT_STOCK("ORDER-101", "재고가 부족합니다", HttpStatus.BAD_REQUEST),
    ORDER_INVALID_QUANTITY("ORDER-102", "유효하지 않은 수량입니다", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS("ORDER-103", "유효하지 않은 주문상태입니다", HttpStatus.BAD_REQUEST),
    EMPTY_ORDER_ITEMS("ORDER-104", "주문 항목이 비어있습니다", HttpStatus.BAD_REQUEST),
    INACTIVE_PRODUCT_IN_ORDER("ORDER-105", "비활성 상품이 포함되어있습니다", HttpStatus.BAD_REQUEST),

    ORDER_ALREADY_CONFIRMED("ORDER-201", "이미 확정된 주문입니다", HttpStatus.CONFLICT),
    ORDER_ALREADY_CANCELED("ORDER-202", "이미 취소된 주문입니다", HttpStatus.CONFLICT),
    CANNOT_CANCEL_ORDER("ORDER-203", "취소할 수 없는 주문입니다", HttpStatus.FORBIDDEN),

    ORDER_CREATION_FAILED("ORDER-901", "주문 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_NUMBER_GENERATION_FAILED("ORDER-902", "주문번호 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    // ── Category ─────────────────────────────────────────────────────────────
    CATEGORY_NOT_FOUND("CATEGORY-001", "카테고리를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    // ── Terminal ─────────────────────────────────────────────────────────────
    TERMINAL_AUTH_REQUIRED("TERMINAL-001", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    TERMINAL_AUTH_INVALID("TERMINAL-002", "잘못된 인증입니다", HttpStatus.UNAUTHORIZED),
    TERMINAL_KEY_MISMATCH("TERMINAL-003", "키가 일치하지 않습니다", HttpStatus.UNAUTHORIZED),
    TERMINAL_INACTIVE("TERMINAL-004", "비활성화된 단말기입니다", HttpStatus.FORBIDDEN),
    TERMINAL_MAINTENANCE("TERMINAL-005", "정비중인 단말기입니다", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
