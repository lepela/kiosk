package dev.lepelaka.kiosk.domain.product.exception;

public class DuplicateProductException extends ProdcutException{
    private final String productName;

    public DuplicateProductException(String productName) {
        super(ProductErrorCode.DUPLICATE_PRODUCT_NAME);
        this.productName = productName;
    }

    public String getMessage() {
        return String.format("%s (상품명 : %s)", super.getMessage(), productName);
    }
}
