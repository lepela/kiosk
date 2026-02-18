package dev.lepelaka.kiosk.domain.product.service;

import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.exception.DuplicateProductException;
import dev.lepelaka.kiosk.domain.product.exception.ProductNotFoundException;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository repository;

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Long register(ProductCreateRequest request) {
        if(repository.existsByName(request.name())) {
            throw new DuplicateProductException(request.name());
        }
        return repository.save(request.toEntity()).getId();
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void modify(Long id, ProductUpdateRequest request) {
        Product product = repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        product.update(request.name(), request.price(), request.quantity(), request.description(), request.imageUrl(), request.category());
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void remove(Long id) {
        repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id)).deactivate();
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse detail(Long id) {
        return repository.findById(id).map(ProductResponse::fromEntity).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public PageResponse<ProductResponse> list(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(ProductResponse::fromEntity));
    }

    public PageResponse<ProductResponse> listByCategory(String category, Pageable pageable) {
        return PageResponse.from(repository.findByCategory(category, pageable).map(ProductResponse::fromEntity));
    }

    @Cacheable(value = "products", key = "'active:page' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PageResponse<ProductResponse> listOnActive(Pageable pageable) {
        return PageResponse.from(repository.findByActiveTrue(pageable).map(ProductResponse::fromEntity));
    }

    @Cacheable(value = "products", key = "'category:' + #category + ':active:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PageResponse<ProductResponse> listByCategoryOnActive(String category, Pageable pageable) {
        return PageResponse.from(repository.findByCategoryAndActiveTrue(category, pageable).map(ProductResponse::fromEntity));
    }


}
