package dev.lepelaka.kiosk.domain.order.repository;

import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByTerminalId(Long terminalId, Pageable pageable);

    Page<Order> findByTerminalIdAndStatus(Long terminalId, OrderStatus status, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("select max(cast(substring(o.orderNumber, 14) as integer)) from Order o where o.orderNumber like :prefix%")
    Optional<Integer> findLastSequenceToday(@Param("prefix") String prefix);

    Page<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

}
