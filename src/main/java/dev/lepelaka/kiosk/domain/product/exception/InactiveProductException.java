package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class InactiveProductException extends ProdcutException {
    private final Long productId;

    public InactiveProductException(Long productId) {
        super(ErrorCode.INACTIVE_PRODUCT);
        this.productId = productId;
    }

    @Override
    public String getMessage() {
        return String.format("%s (상품 ID: %d)", super.getMessage(), productId);
    }
}
