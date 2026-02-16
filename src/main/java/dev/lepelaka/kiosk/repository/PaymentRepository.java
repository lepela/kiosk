package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.entity.Payment;
import dev.lepelaka.kiosk.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);
}
