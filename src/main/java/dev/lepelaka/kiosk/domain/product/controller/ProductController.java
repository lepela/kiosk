package dev.lepelaka.kiosk.domain.product.controller;

import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.service.ProductService;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "상품 API", description = "상품 등록, 수정, 삭제, 조회 API")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "전체 상품 목록 조회 (관리자용)", description = "모든 상품을 페이징하여 조회합니다.")
    @GetMapping("/admin")
    public PageResponse<ProductResponse> getList(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return productService.list(pageable);
    }

    @Operation(summary = "판매 중인 상품 목록 조회", description = "판매 중인(Active) 상품만 페이징하여 조회합니다.")
    @GetMapping("/")
    public PageResponse<ProductResponse> getActiveList(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return productService.listOnActive(pageable);
    }

    @Operation(summary = "카테고리별 상품 목록 조회", description = "특정 카테고리의 상품을 페이징하여 조회합니다.")
    @GetMapping("/category/{categoryId}")
    public PageResponse<ProductResponse> getListBy(
            @Parameter(description = "카테고리명", example = "메인") @PathVariable("categoryId") Long categoryId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return productService.listByCategory(categoryId, pageable);
    }

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "상품이 존재하지 않음")
    @GetMapping("/{id}")
    public ProductResponse get(@Parameter(description = "상품 ID", example = "1") @PathVariable("id") Long id) {
        return productService.detail(id);
    }

    @Operation(summary = "신규 상품 등록", description = "새로운 상품을 등록합니다.")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @PostMapping("/create")
    public ResponseEntity<Long> register(@Valid @RequestBody ProductCreateRequest request) {
        Long id = productService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + id)).body(id);
    }

    @Operation(summary = "상품 정보 수정", description = "기존 상품 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "상품이 존재하지 않음")
    @PutMapping("/{id}")
    public ResponseEntity<Void> modify(
            @Parameter(description = "상품 ID", example = "1") @PathVariable() Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        productService.modify(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "상품 삭제 (Soft Delete)", description = "상품을 판매 중지 상태로 변경합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "상품이 존재하지 않음")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@Parameter(description = "상품 ID", example = "1") @PathVariable() Long id) {
        productService.remove(id);
        return ResponseEntity.ok().build();
    }

}
