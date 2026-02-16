package dev.lepelaka.kiosk.entity;

import dev.lepelaka.kiosk.entity.enums.PaymentMethod;
import dev.lepelaka.kiosk.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Builder

    public Payment(Order order, int amount, PaymentMethod method, PaymentStatus status) {
        this.order = order;
        this.amount = amount;
        this.method = method;
        this.status = status;
    }

    public void complete() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기중인 결제만 완료 가능");
        }
        this.status = PaymentStatus.COMPLETED;
    }

    // 결제 실패
    public void fail() {
        if (this.status == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제는 실패 처리 불가");
        }
        this.status = PaymentStatus.FAILED;
    }

}
