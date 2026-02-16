package dev.lepelaka.kiosk.domain.payment.repository;

import dev.lepelaka.kiosk.domain.payment.entity.Payment;
import dev.lepelaka.kiosk.domain.payment.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);
}
