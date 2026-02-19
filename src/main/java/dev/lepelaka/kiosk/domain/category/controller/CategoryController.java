package dev.lepelaka.kiosk.domain.category.controller;

import dev.lepelaka.kiosk.domain.category.dto.CategoryCreateRequest;
import dev.lepelaka.kiosk.domain.category.dto.CategoryResponse;
import dev.lepelaka.kiosk.domain.category.dto.CategoryUpdateRequest;
import dev.lepelaka.kiosk.domain.category.service.CategoryService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "카테고리 API", description = "카테고리 등록, 수정, 조회, 상태 변경 API")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @Operation(summary = "신규 카테고리 등록", description = "새로운 카테고리를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody @Valid CategoryCreateRequest request) {
        Long id = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(id);
    }

    @Operation(summary = "카테고리 정보 수정", description = "카테고리 이름, 설명, 순서를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "카테고리가 존재하지 않음")
    @PutMapping("/{id}")
    public ResponseEntity<Void> modify(@Parameter(description = "카테고리 ID", example = "1") @PathVariable("id") Long id, @RequestBody @Valid CategoryUpdateRequest request) {
        service.modify(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리 비활성화", description = "카테고리를 노출되지 않도록 비활성화합니다.")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@Parameter(description = "카테고리 ID", example = "1") @PathVariable("id") Long id) {
        service.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리 활성화", description = "카테고리를 다시 노출되도록 활성화합니다.")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@Parameter(description = "카테고리 ID", example = "1") @PathVariable("id") Long id) {
        service.activate(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@Parameter(description = "카테고리 ID", example = "1") @PathVariable("id") Long id) {
        service.remove(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리 상세 조회", description = "카테고리 ID로 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "카테고리가 존재하지 않음")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> detail(@Parameter(description = "카테고리 ID", example = "1") @PathVariable("id") Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(summary = "활성 카테고리 목록 조회", description = "활성화된 카테고리 목록을 순서대로 조회합니다.")
    @GetMapping("/list/active")
    public ResponseEntity<PageResponse<CategoryResponse>> listActive(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listActive(pageable));
    }

    @Operation(summary = "전체 카테고리 목록 조회", description = "모든 카테고리 목록을 순서대로 조회합니다.")
    @GetMapping("/list/all")
    public ResponseEntity<PageResponse<CategoryResponse>> listByDisplayOrder(@ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listByDisplayOrder(pageable));
    }

}
