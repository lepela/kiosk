package dev.lepelaka.kiosk.domain.terminal.entity.enums;

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public enum TerminalStatus {
    ACTIVE("활성"), INACTIVE("비활성"), MAINTENANCE("점검중");
    private final String description;
}

