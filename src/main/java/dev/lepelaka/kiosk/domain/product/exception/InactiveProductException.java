package dev.lepelaka.kiosk.domain.product.exception;

public class InactiveProductException extends ProdcutException {
    private final Long productId;

    public InactiveProductException(Long productId) {
        super(ProductErrorCode.PRODUCT_NOT_FOUND);
        this.productId = productId;
    }

    @Override
    public String getMessage() {
        return String.format("%s (상품 ID: %d)", super.getMessage(), productId) ;
    }
}
