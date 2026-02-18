package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.BusinessException;

public class ProdcutException extends BusinessException {
    protected ProdcutException(ProductErrorCode errorCode) {
        super(errorCode);
    }
    protected ProdcutException(ProductErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
