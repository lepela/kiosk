package dev.lepelaka.kiosk.domain.order.service;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class OrderServiceConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private OrderNumberGenerator orderNumberGenerator;

    private Terminal terminal;
    private Product product;

    @BeforeEach
    void setUp() {
        // 멀티스레드 환경에서 접근해야 하므로 @Transactional 없이 실제 DB에 저장
        terminal = terminalRepository.save(Terminal.builder().name("동시성 테스트 키오스크").build());
        Category category = categoryRepository.save(Category.builder().name("테스트 카테고리").displayOrder(1).build());
        
        product = productRepository.save(Product.builder()
                .name("인기 상품")
                .price(1000)
                .quantity(100) // 재고 100개
                .category(category)
                .build());
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        terminalRepository.deleteAll();
    }

    @DisplayName("동시에 100개의 주문 요청이 들어오면 재고가 정확히 감소해야 한다.")
    @Test
    void concurrentOrderCreation() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger orderSeq = new AtomicInteger(0);

        // 고유한 주문 번호 생성 (중복 에러 방지)
        given(orderNumberGenerator.generate()).willAnswer(invocation -> "ORD-" + System.nanoTime() + "-" + orderSeq.incrementAndGet());

        OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(product.getId(), 1)), terminal.getId());

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.createOrder(request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기

        // then
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("주문 취소 동시 실행 시 재고 정합성 보장.")
    void cancelOrderConcurrentShouldMaintainStockConsistency() throws Exception {
        // given
        int orderCount = 100;
        AtomicInteger orderSeq = new AtomicInteger(0);
        given(orderNumberGenerator.generate()).willAnswer(invocation -> "ORD-CANCEL-" + System.nanoTime() + "-" + orderSeq.incrementAndGet());

        // 100개의 주문을 미리 생성하여 재고를 0으로 만듦
        List<Long> orderIds = new ArrayList<>();
        OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(product.getId(), 1)), terminal.getId());

        for (int i = 0; i < orderCount; i++) {
            orderIds.add(orderService.createOrder(request));
        }

        // 재고 확인 (0이어야 함)
        assertThat(productRepository.findById(product.getId()).orElseThrow().getQuantity()).isEqualTo(0);

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(orderCount);

        // when
        for (Long orderId : orderIds) {
            executorService.submit(() -> {
                try {
                    orderService.cancelOrder(orderId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        // 모든 주문이 취소되었으므로 재고는 다시 100으로 복구되어야 함
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("주문 취소와 새 주문 생성 동시 실행 시 정합성 보장.")
    void cancelAndCreateConcurrentShouldMaintainStockConsistency() throws Exception {
        // given
        int count = 50; // 50개 생성 vs 50개 취소
        AtomicInteger orderSeq = new AtomicInteger(0);
        given(orderNumberGenerator.generate()).willAnswer(invocation -> "ORD-MIX-" + System.nanoTime() + "-" + orderSeq.incrementAndGet());

        // 50개의 주문을 미리 생성 (재고 100 -> 50)
        List<Long> ordersToCancel = new ArrayList<>();
        OrderCreateRequest createRequest = new OrderCreateRequest(List.of(new OrderItemRequest(product.getId(), 1)), terminal.getId());

        for (int i = 0; i < count; i++) {
            ordersToCancel.add(orderService.createOrder(createRequest));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(count * 2); // 생성 50 + 취소 50

        // when
        // 1. 새 주문 생성 스레드 50개 (재고 감소)
        for (int i = 0; i < count; i++) {
            executorService.submit(() -> {
                try {
                    orderService.createOrder(createRequest);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 2. 기존 주문 취소 스레드 50개 (재고 증가)
        for (Long orderId : ordersToCancel) {
            executorService.submit(() -> {
                try {
                    orderService.cancelOrder(orderId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        // 초기(100) - 미리생성(50) - 추가생성(50) + 취소(50) = 50
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(50);
    }
}