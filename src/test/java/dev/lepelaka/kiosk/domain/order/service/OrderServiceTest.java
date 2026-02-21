package dev.lepelaka.kiosk.domain.order.service;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.OrderItem;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.exception.InsufficientStockException;
import dev.lepelaka.kiosk.domain.order.exception.OrderNotFoundException;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.exception.ProductNotFoundException;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TerminalRepository terminalRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @Mock
    private CategoryRepository categoryRepository;

    @DisplayName("주문 생성 시 재고가 감소하고 주문이 저장된다.")
    @Test
    void createOrder() {
        // given
        Long terminalId = 1L;
        Long productId = 100L;
        int requestQuantity = 2;
        int initialStock = 3;

        OrderCreateRequest request = new OrderCreateRequest(
                                List.of(new OrderItemRequest(productId, requestQuantity)), terminalId
        );

        Terminal terminal = mock(Terminal.class);
        
        // Mock 대신 실제 객체 사용 (상태 검증을 위해)
        Product product = Product.builder()
                .name("아메리카노")
                .price(5000)
                .quantity(initialStock)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        given(terminalRepository.findById(terminalId)).willReturn(Optional.of(terminal));
        given(productRepository.findAllByIdWithPessimisticLock(anyList())).willReturn(List.of(product));
        given(orderNumberGenerator.generate()).willReturn("20231010-0001");

        // when
        orderService.createOrder(request);

        // then
        // 1. 실제 재고가 감소했는지 상태 검증 (Mock일 때는 불가능했던 검증)
        assertThat(product.getQuantity()).isEqualTo(initialStock - requestQuantity);
        
        // 2. 주문 저장 메서드가 호출되었는지 검증
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @DisplayName("주문 생성 시 존재하지 않는 상품이 포함되어 있으면 예외가 발생한다.")
    @Test
    void createOrderWithInvalidProduct() {
        // given
        Long terminalId = 1L;
        Long validProductId = 100L;
        Long invalidProductId = 999L;

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(
                        new OrderItemRequest(validProductId, 1),
                        new OrderItemRequest(invalidProductId, 1)
                ),terminalId
        );

        Terminal terminal = mock(Terminal.class);
        Product product = mock(Product.class);

        given(terminalRepository.findById(terminalId)).willReturn(Optional.of(terminal));
        // 유효한 상품 1개만 리턴됨 (요청은 2개)
        given(productRepository.findAllByIdWithPessimisticLock(anyList())).willReturn(List.of(product));
        given(product.getId()).willReturn(validProductId);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @DisplayName("주문 생성 시 재고보다 많은 수량을 요청하면 예외가 발생한다.")
    @Test
    void createOrderWithInsufficientStock() {
        // given
        Long terminalId = 1L;
        Long productId = 100L;
        int requestQuantity = 5;
        int currentStock = 3;

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(productId, requestQuantity)),
                terminalId
        );

        Terminal terminal = mock(Terminal.class);
        
        // Mock 객체는 내부 로직이 실행되지 않으므로, 예외 검증을 위해 실제 객체 사용
        Category category = mock(Category.class);
        Product product = Product.builder()
                .name("테스트상품")
                .price(5000)
                .quantity(currentStock) // 재고 3
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "id", productId);

        given(terminalRepository.findById(terminalId)).willReturn(Optional.of(terminal));
        given(productRepository.findAllByIdWithPessimisticLock(anyList())).willReturn(List.of(product));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining(String.valueOf(productId));
    }

    @DisplayName("주문 취소 시 상품 재고가 복구되고 주문이 취소 상태로 변경된다.")
    @Test
    void cancelOrder() {
        // given
        Long orderId = 1L;
        Long productId = 100L;
        int quantity = 2;

        Order order = mock(Order.class);
        OrderItem orderItem = mock(OrderItem.class);
        Product product = mock(Product.class);

        // Order Mock 설정
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.isCancellable()).willReturn(true);
        given(order.getOrderItems()).willReturn(List.of(orderItem));

        // OrderItem Mock 설정
        given(orderItem.getProductId()).willReturn(productId);
        given(orderItem.getQuantity()).willReturn(quantity);

        // Product Mock 설정
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        orderService.cancelOrder(orderId);

        // then
        // 1. 재고 증가(복구) 호출 검증
        verify(product, times(1)).restore(quantity);
        
        // 2. 주문 취소 처리 호출 검증
        verify(order, times(1)).cancel();
    }

    // NPE 테스트
    @Test
    void getOrderWithNullOrder() {
        // given
//        Long orderId = null;
        String name = null;
        assertThatThrownBy(() -> orderService.getOrder(name)).isInstanceOf(OrderNotFoundException.class);
    }
}