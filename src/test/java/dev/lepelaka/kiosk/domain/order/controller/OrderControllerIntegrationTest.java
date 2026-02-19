package dev.lepelaka.kiosk.domain.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderNumberGenerator orderNumberGenerator; // Redis 의존성 제거를 위해 Mocking

    private Terminal terminal;
    private Product product;

    @BeforeEach
    void setUp() {
        terminal = terminalRepository.save(Terminal.builder().name("강남점 키오스크 1번").status(TerminalStatus.ACTIVE).build());
        Category category = categoryRepository.save(Category.builder().name("커피").displayOrder(1).build());
        product = productRepository.save(Product.builder()
                .name("아메리카노")
                .price(5000)
                .quantity(100)
                .category(category)
                .build());

        given(orderNumberGenerator.generate()).willReturn("20231010-0001");
    }

    @DisplayName("주문 생성부터 조회, 취소까지의 전체 흐름을 검증한다.")
    @Test
    void orderFullCycle() throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(product.getId(), 2);
        // 1. 주문 생성
        OrderCreateRequest request = new OrderCreateRequest(List.of(orderItemRequest), terminal.getId());
        
        String location = mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.orderNumber").value("20231010-0001"))
                .andReturn().getResponse().getHeader("Location");

        // Location: http://localhost/api/v1/order/1 -> ID 추출
        String idStr = location.substring(location.lastIndexOf("/") + 1);
        Long orderId = Long.parseLong(idStr);

        // 2. 주문 조회 (주문 번호로)
        mockMvc.perform(get("/api/v1/order/{orderNumber}", "20231010-0001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(10000));

        // 3. 주문 취소
        mockMvc.perform(post("/api/v1/order/{orderId}/cancel", orderId))
                .andDo(print())
                .andExpect(status().isOk());

        // 4. DB 상태 검증
        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }
}