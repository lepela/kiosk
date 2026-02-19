package dev.lepelaka.kiosk.domain.product.exception;

public class InvalidQuantityException extends ProdcutException{
    private Long id;
    private int quantity;
    public InvalidQuantityException(Long id, int quantity) {
        super(ProductErrorCode.INVALID_QUANTITY);
        this.id = id;
        this.quantity = quantity;
    }

    public String getMessage() {
        return String.format("%s, (상품 ID : %d) %d는 유효하지 않은 수량입니다.", super.getMessage(), id, quantity);
    }
}
