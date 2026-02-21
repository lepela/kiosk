package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;

import java.util.Collection;

public class ProductNotFoundException extends ProdcutException {
    public ProductNotFoundException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_FOUND);
    }

    public ProductNotFoundException(Collection<Long> productIds) {
        super(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
