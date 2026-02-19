package dev.lepelaka.kiosk.domain.order.repository;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.OrderItem;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    private Order order;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Kiosk 생성
        Terminal terminal = Terminal.builder()
                .location("매장1-1호기")
                .status(TerminalStatus.ACTIVE)
                .build();
        terminalRepository.save(terminal);

        // Order 생성
        order = Order.builder()
                .orderNumber("ORD-001")
                .totalAmount(15000)
                .status(OrderStatus.PENDING)
                .terminal(terminal)
                .build();
        orderRepository.save(order);

        // Product 생성
        product1 = Product.builder()
                .name("짜장면")
                .price(7000)
                .category(Category.builder().name("메인").build())
                .build();

        product2 = Product.builder()
                .name("짬뽕")
                .price(8000)
                .category(Category.builder().name("메인").build())
                .build();

        productRepository.saveAll(List.of(product1, product2));
    }

    @Test
    @DisplayName("주문 아이템 저장 및 조회")
    void save_and_find() {
        // given
        OrderItem orderItem = createOrderItem(order, product1, 2, 7000);

        // when
        OrderItem saved = orderItemRepository.save(orderItem);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(2);
        assertThat(saved.getPrice()).isEqualTo(7000);
        assertThat(saved.getTotalPrice()).isEqualTo(14000);
    }

    @Test
    @DisplayName("주문별 아이템 조회")
    void findByOrderId() {
        // given
        OrderItem item1 = createOrderItem(order, product1, 2, 7000);
        OrderItem item2 = createOrderItem(order, product2, 1, 8000);
        orderItemRepository.saveAll(List.of(item1, item2));

        // when
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        // then
        assertThat(items).hasSize(2);
    }

    @Test
    @DisplayName("상품별 주문 내역 조회")
    void findByProductId() {
        // given
        OrderItem item1 = createOrderItem(order, product1, 2, 7000);
        OrderItem item2 = createOrderItem(order, product1, 1, 7000);
        orderItemRepository.saveAll(List.of(item1, item2));

        // when
        List<OrderItem> items = orderItemRepository.findByProductId(product1.getId());

        // then
        assertThat(items).hasSize(2);
        assertThat(items)
                .extracting("productId")
                .containsOnly(product1.getId());
    }

    // 헬퍼 메서드
    private OrderItem createOrderItem(Order order, Product product,
                                      int quantity, int price) {
        return OrderItem.builder()
                .order(order)
                .productId(product.getId())
                .productName(product.getName())
                .quantity(quantity)
                .price(price)
                .build();
    }
}