package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    private Terminal terminal;

    @BeforeEach
    void setUp() {
        terminal = Terminal.builder()
                .location("매장1-1호기")
                .status(TerminalStatus.ACTIVE)
                .build();
        terminalRepository.save(terminal);
    }

    @Test
    @DisplayName("주문 저장 및 조회")
    void save_and_find() {
        // given
        Order order = createOrder("ORD-001", 10000, OrderStatus.PENDING);

        // when
        Order saved = orderRepository.save(order);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(saved.getTotalAmount()).isEqualTo(10000);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문번호로 조회")
    void findByOrderNumber() {
        // given
        Order order = createOrder("ORD-001", 10000, OrderStatus.PENDING);
        orderRepository.save(order);

        // when
        Optional<Order> found = orderRepository.findByOrderNumber("ORD-001");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    @DisplayName("터미널별 주문 조회")
    void findByTerminalId() {
        // given
        Order order1 = createOrder("ORD-001", 10000, OrderStatus.PENDING);
        Order order2 = createOrder("ORD-002", 15000, OrderStatus.COMPLETED);
        orderRepository.saveAll(List.of(order1, order2));

        // when
        Page<Order> orders = orderRepository.findByTerminalId(terminal.getId(), PageRequest.of(0, 10));

        // then
        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("터미널별 + 상태별 조회")
    void findByTerminalIdAndStatus() {
        // given
        Order order1 = createOrder("ORD-001", 10000, OrderStatus.PENDING);
        Order order2 = createOrder("ORD-002", 15000, OrderStatus.COMPLETED);
        Order order3 = createOrder("ORD-003", 20000, OrderStatus.PENDING);
        orderRepository.saveAll(List.of(order1, order2, order3));

        // when
        Page<Order> pendingOrders = orderRepository
                .findByTerminalIdAndStatus(terminal.getId(), OrderStatus.PENDING, PageRequest.of(0, 10));

        // then
        assertThat(pendingOrders).hasSize(2);
        assertThat(pendingOrders)
                .extracting("status")
                .containsOnly(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("상태별 조회")
    void findByStatus() {
        // given
        Order order1 = createOrder("ORD-001", 10000, OrderStatus.PENDING);
        Order order2 = createOrder("ORD-002", 15000, OrderStatus.PENDING);
        Order order3 = createOrder("ORD-003", 20000, OrderStatus.COMPLETED);
        orderRepository.saveAll(List.of(order1, order2, order3));

        // when
        Page<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING, PageRequest.of(0,10));

        // then
        assertThat(pendingOrders).hasSize(2);
    }

    @Test
    @DisplayName("오늘 주문번호 마지막 시퀀스 조회")
    void findLastSequenceToday() {
        // given
        String today = "20250216";
        String prefix = "ORD-" + today + "-";

        Order order1 = createOrder("ORD-20250216-0001", 10000, OrderStatus.PENDING);
        Order order2 = createOrder("ORD-20250216-0002", 15000, OrderStatus.PENDING);
        Order order3 = createOrder("ORD-20250216-0003", 20000, OrderStatus.PENDING);
        Order order4 = createOrder("ORD-20250217-0001", 10000, OrderStatus.PENDING);
        orderRepository.saveAll(List.of(order1, order2, order3, order4));

        // when
        Optional<Integer> lastSeq = orderRepository.findLastSequenceToday(prefix);

        // then
        assertThat(lastSeq).isPresent();
        assertThat(lastSeq.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("날짜 범위 조회")
    void findByCreatedAtBetween() {
        // given
        Order order1 = createOrder("ORD-001", 10000, OrderStatus.PENDING);
        Order order2 = createOrder("ORD-002", 15000, OrderStatus.PENDING);
        orderRepository.saveAll(List.of(order1, order2));

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // when
        Page<Order> orders = orderRepository.findByCreatedAtBetween(start, end, PageRequest.of(0, 10));

        // then
        assertThat(orders).hasSize(2);
    }

    // 헬퍼 메서드
    private Order createOrder(String orderNumber, int totalAmount, OrderStatus status) {
        return Order.builder()
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .status(status)
                .terminal(terminal)
                .build();
    }
}