package dev.lepelaka.kiosk.domain.terminal.exception;

import dev.lepelaka.kiosk.global.exception.BusinessException;

public class TerminalException extends BusinessException {
    public TerminalException(TerminalErrorCode errorCode) {
        super(errorCode);
    }
}
