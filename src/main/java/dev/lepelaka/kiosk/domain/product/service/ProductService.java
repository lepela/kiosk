package dev.lepelaka.kiosk.domain.product.service;

import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository repository;

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Long register(ProductCreateRequest request) {
        return repository.save(request.toEntity()).getId();
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void modify(Long id, ProductUpdateRequest request) {
        Product product = repository.findById(id).orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다"));
        product.update(request.name(), request.price(), request.quantity(), request.description(), request.imageUrl(), request.category());
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        repository.findById(id).orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다")).deactivate();
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse detail(Long id) {
        return repository.findById(id).map(ProductResponse::fromEntity).orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다"));
    }

    @Cacheable(value = "products", key = "'all'")
    public List<ProductResponse> list() {
        return repository.findAll().stream().map(ProductResponse::fromEntity).toList();
    }

    @Cacheable(value = "products", key = "'active'")
    public List<ProductResponse> listOnActive() {
        return repository.findByActiveTrue().stream().map(ProductResponse::fromEntity).toList();
    }

    @Cacheable(value = "products", key = "'category:' + #category")
    public List<ProductResponse> listByCategory(String category) {
        return repository.findByCategory(category).stream().map(ProductResponse::fromEntity).toList();
    }

    @Cacheable(value = "products", key = "'category:' + #category + ':active'")
    public List<ProductResponse> listByCategoryOnActive(String category) {
        return repository.findByCategoryAndActiveTrue(category).stream().map(ProductResponse::fromEntity).toList();
    }


}
