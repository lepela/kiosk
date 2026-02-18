package dev.lepelaka.kiosk.domain.order.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    CANCELED("취소됨", Set.of()),
    COMPLETED("완료됨", Set.of()),
    CONFIRMED("확정됨", Set.of(COMPLETED)),
    PENDING("대기중", Set.of(CONFIRMED, CANCELED)),;

    private final String description;
    private final Set<OrderStatus> allowedTransitions;
    
    public boolean canTransitionTo(OrderStatus newStatus) {
        return allowedTransitions.contains(newStatus);
    }
}
