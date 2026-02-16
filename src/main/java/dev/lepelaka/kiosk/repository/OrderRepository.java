package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.entity.Kiosk;
import dev.lepelaka.kiosk.entity.Order;
import dev.lepelaka.kiosk.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByKioskId(Long kioskId);

    List<Order> findByKioskIdAndStatus(Long kioskId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    @Query("select max(cast(substring(o.orderNumber, 14) as integer)) from Order o where o.orderNumber like :prefix%")
    Optional<Integer> findLastSequenceToday(@Param("prefix") String prefix);

    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
