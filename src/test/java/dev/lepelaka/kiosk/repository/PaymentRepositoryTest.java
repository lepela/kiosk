package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.entity.*;
import dev.lepelaka.kiosk.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KioskRepository kioskRepository;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        // Kiosk 생성
        Kiosk kiosk = Kiosk.builder()
                .location("매장1-1호기")
                .status(KioskStatus.ACTIVE)
                .build();
        kioskRepository.save(kiosk);

        // Order 생성
        order1 = Order.builder()
                .orderNumber("ORD-001")
                .totalAmount(10000)
                .status(OrderStatus.PENDING)
                .kiosk(kiosk)
                .build();

        order2 = Order.builder()
                .orderNumber("ORD-002")
                .totalAmount(15000)
                .status(OrderStatus.PENDING)
                .kiosk(kiosk)
                .build();

        orderRepository.saveAll(List.of(order1, order2));
    }

    @Test
    @DisplayName("결제 저장 및 조회")
    void save_and_find() {
        // given
        Payment payment = createPayment(order1, 10000,
                PaymentMethod.CARD,
                PaymentStatus.COMPLETED);

        // when
        Payment saved = paymentRepository.save(payment);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualTo(10000);
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("주문 ID로 결제 조회")
    void findByOrderId() {
        // given
        Payment payment = createPayment(order1, 10000,
                PaymentMethod.CARD,
                PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // when
        Optional<Payment> found = paymentRepository.findByOrderId(order1.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOrder().getId()).isEqualTo(order1.getId());
    }

    @Test
    @DisplayName("상태별 결제 조회")
    void findByStatus() {
        // given
        Payment payment1 = createPayment(order1, 10000,
                PaymentMethod.CARD,
                PaymentStatus.COMPLETED);
        Payment payment2 = createPayment(order2, 15000,
                PaymentMethod.CASH,
                PaymentStatus.PENDING);
        paymentRepository.saveAll(List.of(payment1, payment2));

        // when
        List<Payment> completedPayments = paymentRepository
                .findByStatus(PaymentStatus.COMPLETED);

        // then
        assertThat(completedPayments).hasSize(1);
        assertThat(completedPayments.get(0).getStatus())
                .isEqualTo(PaymentStatus.COMPLETED);
    }

    // 헬퍼 메서드
    private Payment createPayment(Order order, int amount,
                                  PaymentMethod method,
                                  PaymentStatus status) {
        return Payment.builder()
                .order(order)
                .amount(amount)
                .method(method)
                .status(status)
                .build();
    }
}