package dev.lepelaka.kiosk.entity;

import dev.lepelaka.kiosk.entity.enums.KioskStatus;
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
public class Kiosk extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KioskStatus status;

    private LocalDateTime lastHeartbeat;

    @OneToMany(mappedBy = "kiosk")
    private List<Order> orders = new ArrayList<>();  // 초기화!

    @Builder
    public Kiosk(String location, KioskStatus status) {
        this.location = location;
        this.status = status;
    }
}