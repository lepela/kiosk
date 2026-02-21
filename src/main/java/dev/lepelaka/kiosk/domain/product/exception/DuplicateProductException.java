package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class DuplicateProductException extends ProdcutException {
    private final String productName;

    public DuplicateProductException(String productName) {
        super(ErrorCode.DUPLICATE_PRODUCT_NAME);
        this.productName = productName;
    }

    @Override
    public String getMessage() {
        return String.format("%s (상품명 : %s)", super.getMessage(), productName);
    }
}
