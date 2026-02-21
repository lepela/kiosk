package dev.lepelaka.kiosk.domain.terminal.entity;

import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "orders")  // 순환참조 방지
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Terminal extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TerminalStatus status = TerminalStatus.ACTIVE;

    private LocalDateTime lastHeartbeat;

    @OneToMany(mappedBy = "terminal", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @Builder
    public Terminal(String name, String keyHash) {
        this.name = name;
        // 테스트 코드 호환성을 위해 keyHash가 없으면 임의의 값 생성
        this.keyHash = keyHash != null ? keyHash : UUID.randomUUID().toString();
    }

    public void invalidate() {
        status = TerminalStatus.INACTIVE;
    }
    public void maintenance () {
        status = TerminalStatus.MAINTENANCE;
    }
    public void heartbeat(LocalDateTime now) {
        lastHeartbeat = now;
    }
    public boolean isAlive(LocalDateTime now, Duration timeout) {
        return lastHeartbeat != null && lastHeartbeat.isAfter(now.minus(timeout));
    }
}