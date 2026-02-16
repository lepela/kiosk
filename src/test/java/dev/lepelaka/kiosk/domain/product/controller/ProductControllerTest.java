package dev.lepelaka.kiosk.domain.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.service.ProductService;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    @DisplayName("신규 상품을 등록한다.")
    void register() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "짜장면", 7000, 100, "맛있는 짜장면", "url", "메인"
        );

        given(productService.register(any(ProductCreateRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/v1/products/create")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/products/1"))
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("상품 정보를 수정한다.")
    void modify() throws Exception {
        // given
        Long productId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "쟁반짜장", 8000, 50, "더 맛있는 짜장", "new_url", "메인"
        );

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
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
        Long productId = 1L;

        // when & then
        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 상세 정보를 조회한다.")
    void getDetail() throws Exception {
        // given
        Long productId = 1L;
        ProductResponse response = new ProductResponse(
                productId, "짜장면", 7000, 100, "맛있는 짜장면", "url", "메인"
        );

        given(productService.detail(productId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("짜장면"))
                .andExpect(jsonPath("$.price").value(7000));
    }

    @Test
    @DisplayName("판매 중인 상품 목록을 조회한다. (페이징)")
    void getActiveList() throws Exception {
        // given
        List<ProductResponse> content = List.of(
                new ProductResponse(1L, "짜장면", 7000, 100, "맛있는 짜장면", "url", "메인"),
                new ProductResponse(2L, "짬뽕", 8000, 100, "맛있는 짬뽕", "url", "메인")
        );
        

        PageResponse<ProductResponse> response = PageResponse.from(new PageImpl<>(content, PageRequest.of(0, 10), 2));

        given(productService.listOnActive(any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/products/")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("짜장면"))
                .andExpect(jsonPath("$.content[1].name").value("짬뽕"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}