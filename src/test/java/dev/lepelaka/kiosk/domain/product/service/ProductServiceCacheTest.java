package dev.lepelaka.kiosk.domain.product.service;

import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class ProductServiceCacheTest {

    @Autowired
    private ProductService productService;

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
    @DisplayName("상품 상세 조회 시 캐시가 적용된다.")
    void cache_hit() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "짜장면", 7000, 100, "맛있는 짜장면", "url", "메인"
        );
        Long savedId = productService.register(request);
        log.info("=== [1] 상품 등록 완료 (ID: {}) ===", savedId);

        // when
        log.info("=== [2] 첫 번째 조회 (DB 조회 발생 예상) ===");
        ProductResponse firstResponse = productService.detail(savedId);
        
        // 캐시 확인
        Cache.ValueWrapper cachedValue = Objects.requireNonNull(cacheManager.getCache("products")).get(savedId);
        log.info("=== [2.1] 캐시 저장 여부 확인: {} ===", cachedValue != null ? "저장됨" : "저장안됨");
        assertThat(cachedValue).isNotNull();

        log.info("=== [3] 두 번째 조회 (캐시 조회 예상 - DB 쿼리 없어야 함) ===");
        ProductResponse secondResponse = productService.detail(savedId);

        // then
        assertThat(firstResponse.id()).isEqualTo(secondResponse.id());
        log.info("=== [4] 테스트 종료 ===");
    }

    @Test
    @DisplayName("상품 수정 시 캐시가 무효화된다.")
    void cache_evict() {
        // given
        ProductCreateRequest createRequest = new ProductCreateRequest(
                "짜장면", 7000, 100, "맛있는 짜장면", "url", "메인"
        );
        Long savedId = productService.register(createRequest);
        log.info("=== [1] 상품 등록 완료 (ID: {}) ===", savedId);

        // 1. 조회하여 캐시 생성
        productService.detail(savedId);
        log.info("=== [2] 초기 조회 완료 (캐시 생성됨) ===");
        assertThat(Objects.requireNonNull(cacheManager.getCache("products")).get(savedId)).isNotNull();

        // when
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                "쟁반짜장", 8000, 50, "더 맛있는 짜장", "new_url", "메인"
        );
        log.info("=== [3] 상품 수정 요청 (캐시 삭제 예상) ===");
        productService.modify(savedId, updateRequest);

        // then
        // 캐시가 비워졌는지 확인
        Cache.ValueWrapper cachedValue = Objects.requireNonNull(cacheManager.getCache("products")).get(savedId);
        log.info("=== [3.1] 캐시 삭제 여부 확인: {} ===", cachedValue == null ? "삭제됨 (성공)" : "삭제안됨 (실패)");
        assertThat(cachedValue).isNull();

        log.info("=== [4] 재조회 (DB 조회 발생 및 변경된 값 확인) ===");
        ProductResponse response = productService.detail(savedId);
        assertThat(response.name()).isEqualTo("쟁반짜장");
        log.info("=== [5] 테스트 종료 ===");
    }
}