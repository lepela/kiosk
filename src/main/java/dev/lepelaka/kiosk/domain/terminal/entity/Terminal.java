package dev.lepelaka.kiosk.domain.terminal.entity;

import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TerminalStatus status;

    private LocalDateTime lastHeartbeat;

    @OneToMany(mappedBy = "terminal")
    private List<Order> orders = new ArrayList<>();

    @Builder
    public Terminal(String location, TerminalStatus status) {
        this.location = location;
        this.status = status;
    }
}