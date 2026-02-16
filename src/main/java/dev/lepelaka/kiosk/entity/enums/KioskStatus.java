package dev.lepelaka.kiosk.entity.enums;

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public enum KioskStatus {
    ACTIVE("활성"), INACTIVE("비활성"), MAINTENANCE("점검중");
    private final String description;
}

