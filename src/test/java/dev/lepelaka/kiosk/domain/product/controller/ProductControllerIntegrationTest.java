package dev.lepelaka.kiosk.domain.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void tearDown() {
        productRepository.deleteAllInBatch();
        Objects.requireNonNull(cacheManager.getCache("products")).clear();
    }

    @Test
    @DisplayName("신규 상품을 등록한다.")
    void register() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "짜장면", 7000, 100, "맛있는 짜장면", "url", "메인"
        );

        // when & then
        mockMvc.perform(post("/api/v1/products/create")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    @DisplayName("상품 정보를 수정한다.")
    void modify() throws Exception {
        // given
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();
        productRepository.save(product);

        ProductUpdateRequest request = new ProductUpdateRequest(
                "쟁반짜장", 8000, 50, "더 맛있는 짜장", "new_url", "메인"
        );

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", product.getId())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품을 삭제한다.")
    void remove() throws Exception {
        // given
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();
        productRepository.save(product);

        // when & then
        mockMvc.perform(delete("/api/v1/products/{id}", product.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 상세 정보를 조회한다.")
    void getDetail() throws Exception {
        // given
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();
        productRepository.save(product);

        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("짜장면"))
                .andExpect(jsonPath("$.price").value(7000));
    }

    @Test
    @DisplayName("판매 중인 상품 목록을 조회한다. (페이징)")
    void getActiveList() throws Exception {
        // given
        Product product1 = Product.builder().name("짜장면").price(7000).quantity(100).description("맛있는 짜장면").imageUrl("url").category("메인").build();
        Product product2 = Product.builder().name("짬뽕").price(8000).quantity(100).description("맛있는 짬뽕").imageUrl("url").category("메인").build();
        productRepository.saveAll(java.util.List.of(product1, product2));

        // when & then
        mockMvc.perform(get("/api/v1/products/")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("짬뽕"))
                .andExpect(jsonPath("$.content[1].name").value("짜장면"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}