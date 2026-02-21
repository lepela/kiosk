package dev.lepelaka.kiosk.domain.terminal.exception;

import dev.lepelaka.kiosk.global.exception.BusinessException;
import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class TerminalException extends BusinessException {
    public TerminalException(ErrorCode errorCode) {
        super(errorCode);
    }
}
