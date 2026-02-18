package dev.lepelaka.kiosk.domain.order.service;

import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.exception.InsufficientStockException;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @MockitoBean
    private OrderNumberGenerator orderNumberGenerator;

    @DisplayName("주문 생성 시 재고가 차감되고 주문 내역이 저장된다.")
    @Test
    void createOrder() {
        // given
        Terminal terminal = terminalRepository.save(new Terminal("강남점 키오스크 1번"));
        Product product = productRepository.save(createProduct("아메리카노", 5000, 10, "메인"));

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                terminal.getId()
        );

        given(orderNumberGenerator.generate()).willReturn("20231010-0001");

        // when
        Long orderId = orderService.createOrder(request);

        // then
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();
        
        // 1. 주문 기본 정보 검증
        assertThat(savedOrder.getOrderNumber()).isEqualTo("20231010-0001");
        assertThat(savedOrder.getTerminal().getId()).isEqualTo(terminal.getId());
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING); // 초기 상태 가정
        
        // 2. 주문 상품 및 총액 검증
        assertThat(savedOrder.getOrderItems()).hasSize(1);
        assertThat(savedOrder.getTotalAmount()).isEqualTo(10000); // 5000 * 2

        // 3. 재고 차감 검증
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(8); // 10 - 2
    }

    @DisplayName("동일한 상품을 여러 번 나누어 주문해도 수량이 합산되어 처리된다.")
    @Test
    void createOrderWithDuplicateItems() {
        // given
        Terminal terminal = terminalRepository.save(new Terminal("강남점 키오스크 1번"));
        Product product = productRepository.save(createProduct("아메리카노", 5000, 10, "메인"));

        // 같은 상품을 2개, 3개로 나누어 요청 (총 5개)
        OrderCreateRequest request = new OrderCreateRequest(
                List.of(
                        new OrderItemRequest(product.getId(), 2),
                        new OrderItemRequest(product.getId(), 3)
                ),
                terminal.getId()
        );

        given(orderNumberGenerator.generate()).willReturn("20231010-0002");

        // when
        Long orderId = orderService.createOrder(request);

        // then
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();
        
        // 주문 항목은 상품별로 하나로 합쳐져야 함 (로직에 따라 다를 수 있으나, 현재 서비스 로직상 상품 루프를 돌며 생성하므로 1개)
        assertThat(savedOrder.getOrderItems()).hasSize(1);
        assertThat(savedOrder.getOrderItems().getFirst().getQuantity()).isEqualTo(5);

        // 재고 확인
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(5); // 10 - 5
    }

    @DisplayName("재고보다 많은 수량을 주문하면 예외가 발생한다.")
    @Test
    void createOrderWithInsufficientStock() {
        // given
        Terminal terminal = terminalRepository.save(new Terminal("강남점 키오스크 1번"));
        Product product = productRepository.save(createProduct("한정판 텀블러", 30000, 1, "메인")); // 재고 1개

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)), // 2개 요청
                terminal.getId()
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @DisplayName("주문 취소 시 재고가 복구되고 주문 상태가 취소로 변경된다.")
    @Test
    void cancelOrder() {
        // given
        Terminal terminal = terminalRepository.save(new Terminal("강남점 키오스크 1번"));
        Product product = productRepository.save(createProduct("아메리카노", 5000, 5, "이벤트"));

        // 주문 생성 (재고 5 -> 3)
        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 2)),
                terminal.getId()
        );
        given(orderNumberGenerator.generate()).willReturn("20231010-0003");
        Long orderId = orderService.createOrder(request);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);

        Product restoredProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(restoredProduct.getQuantity()).isEqualTo(5); // 다시 5로 복구
    }

    private Product createProduct(String name, int price, int quantity, String category) {
        return Product.builder().name(name).price(price).quantity(quantity).category(category).build();
    }
}