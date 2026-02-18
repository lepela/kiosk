package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT-001", "상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    INVALID_PRODUCT_NAME("PRODUCT-101", "유효하지 않은 상품명입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PRICE("PRODUCT-102", "유효하지 않은 가격입니다", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY("PRODUCT-103", "유효하지 않은 수량입니다", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY("PRODUCT-104", "유효하지 않은 카테고리입니다", HttpStatus.BAD_REQUEST),

    INACTIVE_PRODUCT("PRODUCT-201", "비활성 상태의 상품입니다.", HttpStatus.FORBIDDEN),
    DUPLICATE_PRODUCT_NAME("PRODUCT-202", "이미 존재하는 상품명입니다", HttpStatus.CONFLICT),
    CANNOT_DELETE_PRODUCT("PRODUCT-203", "삭제할 수 없는 상품입니다.", HttpStatus.FORBIDDEN),

    PRODUCT_CREATION_FAILED("PRODUCT-901", "상품 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
