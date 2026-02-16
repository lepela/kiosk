package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.domain.product.entity.Product;
import static org.assertj.core.api.Assertions.*;

import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@DataJpaTest
public class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    private Pageable pageable = PageRequest.of(0, 10);

    @Test
    public void testExist() {
        assertThat(productRepository).isNotNull();
    }

    @Test
    @DisplayName("상품 저장 및 조회")
    void save_and_find() {
        // given
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .category("메인")
                .description("맛있는 짜장면")
                .imageUrl("http://example.com/jjajang.jpg")
                .build();

        // when
        Product saved = productRepository.save(product);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("짜장면");
        assertThat(saved.getPrice()).isEqualTo(7000);
    }

    @Test
    @DisplayName("카테고리별 조회")
    void findByCategory() {
        // given
        Product product1 = createProduct("짜장면", 7000, "메인");
        Product product2 = createProduct("짬뽕", 8000, "메인");
        Product product3 = createProduct("콜라", 2000, "음료");

        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        Page<Product> mainProducts = productRepository.findByCategory("메인", pageable);

        // then
        assertThat(mainProducts).hasSize(2);
        assertThat(mainProducts)
                .extracting("name")
                .containsExactlyInAnyOrder("짜장면", "짬뽕");
    }

    @Test
    @DisplayName("판매 가능한 상품만 조회")
    void findByActiveTrue() {
        // given
        Product product1 = createProduct("짜장면", 7000, "메인");
        Product product2 = createProduct("짬뽕", 8000, "메인");

        productRepository.saveAll(List.of(product1, product2));

        // when
        Page<Product> activeProducts = productRepository.findByActiveTrue(pageable);

        // then
        assertThat(activeProducts).hasSize(2);
    }

    @Test
    @DisplayName("카테고리 + 판매가능 조회")
    void findByCategoryAndActiveTrue() {
        // given
        Product product1 = createProduct("짜장면", 7000, "메인");
        Product product2 = createProduct("짬뽕", 8000, "메인");
        Product product3 = createProduct("콜라", 2000, "음료");

        product2.deactivate();
        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        Page<Product> mainProducts = productRepository
                .findByCategoryAndActiveTrue("메인", pageable);

        // then
        assertThat(mainProducts).hasSize(1);
    }

    // 헬퍼 메서드
    private Product createProduct(String name, int price, String category) {
        return Product.builder()
                .name(name)
                .price(price)
                .category(category)
                .build();
    }
}
