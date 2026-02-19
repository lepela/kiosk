package dev.lepelaka.kiosk.domain.product.repository;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
    @Autowired
    private CategoryRepository categoryRepository;

    private Pageable pageable = PageRequest.of(0, 10);

    private Category mainCategory;
    private Category sideCategory;

    @Test
    public void testExist() {
        assertThat(productRepository).isNotNull();
    }

    @BeforeEach
    public void setup() {
        mainCategory = categoryRepository.save(Category.builder().name("메인").description("메인 메뉴입니다.").displayOrder(1).build());
        sideCategory = categoryRepository.save(Category.builder().name("사이드").description("사이드 메뉴입니다.").displayOrder(2).build());
    }

    @Test
    @DisplayName("상품 저장 및 조회")
    void save_and_find() {
        // given
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .description("맛있는 짜장면")
                .imageUrl("http://example.com/jjajang.jpg")
                .category(mainCategory)
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
        Product product1 = createProduct("짜장면", 7000, mainCategory);
        Product product2 = createProduct("짬뽕", 8000, mainCategory);
        Product product3 = createProduct("콜라", 2000, sideCategory);

        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        Page<Product> mainProducts = productRepository.findByCategory(mainCategory, pageable);

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
        Product product1 = createProduct("짜장면", 7000, mainCategory);
        Product product2 = createProduct("짬뽕", 8000,mainCategory);

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
        Product product1 = createProduct("짜장면", 7000, mainCategory);
        Product product2 = createProduct("짬뽕", 8000, mainCategory);
        Product product3 = createProduct("콜라", 2000, sideCategory);

        product2.deactivate();
        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        Page<Product> mainProducts = productRepository
                .findByCategoryAndActiveTrue(mainCategory, pageable);

        // then
        assertThat(mainProducts).hasSize(1);
    }

    // 헬퍼 메서드
    private Product createProduct(String name, int price, Category category) {
        return Product.builder()
                .name(name)
                .price(price)
                .category(category)
                .build();
    }
}
