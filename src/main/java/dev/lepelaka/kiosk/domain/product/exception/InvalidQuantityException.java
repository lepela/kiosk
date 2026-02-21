package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class InvalidQuantityException extends ProductException {
    private final Long id;
    private final int quantity;

    public InvalidQuantityException(Long id, int quantity) {
        super(ErrorCode.INVALID_QUANTITY);
        this.id = id;
        this.quantity = quantity;
    }

    @Override
    public String getMessage() {
        return String.format("%s, (상품 ID : %d) %d는 유효하지 않은 수량입니다.", super.getMessage(), id, quantity);
    }
}
