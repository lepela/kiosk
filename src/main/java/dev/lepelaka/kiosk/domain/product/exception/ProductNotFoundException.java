package dev.lepelaka.kiosk.domain.product.exception;

import java.util.Collection;

public class ProductNotFoundException extends ProdcutException {
    public ProductNotFoundException(Long productId) {
        super(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    public ProductNotFoundException(Collection<Long> productIds) {
        super(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
