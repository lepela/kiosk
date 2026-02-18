package dev.lepelaka.kiosk.domain.order.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class InsufficientStockException extends OrderException {
    private final Long productId;
    private final int requested;
    private final int available;
    public InsufficientStockException(Long productId, int requested, int available) {
        super(OrderErrorCode.INSUFFICIENT_STOCK);
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }
    @Override
    public String getMessage() {
        return String.format("%s (상품 ID : %d, 요청 : %d, 재고 : %d", super.getMessage(), productId, requested, available) ;
    }

    public Map<String, Object> getDetails() {
        return Map.of("productId", productId, "requested", requested, "available", available);
    }
}
