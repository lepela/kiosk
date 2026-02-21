package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.BusinessException;
import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class ProdcutException extends BusinessException {
    protected ProdcutException(ErrorCode errorCode) {
        super(errorCode);
    }
    protected ProdcutException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
