package dev.lepelaka.kiosk.domain.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lepelaka.kiosk.domain.category.dto.CategoryCreateRequest;
import dev.lepelaka.kiosk.domain.category.dto.CategoryResponse;
import dev.lepelaka.kiosk.domain.category.dto.CategoryUpdateRequest;
import dev.lepelaka.kiosk.domain.category.service.CategoryService;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @DisplayName("신규 카테고리를 등록한다.")
    @Test
    void create() throws Exception {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest("커피", "맛있는 커피", 1);
        given(categoryService.create(any(CategoryCreateRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/categories/1"))
                .andExpect(jsonPath("$").value(1L));
    }

    @DisplayName("카테고리 정보를 수정한다.")
    @Test
    void modify() throws Exception {
        // given
        Long id = 1L;
        CategoryUpdateRequest request = new CategoryUpdateRequest("라떼", "우유 커피", 2);

        // when & then
        mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(categoryService).modify(eq(id), any(CategoryUpdateRequest.class));
    }

    @DisplayName("카테고리를 비활성화한다.")
    @Test
    void deactivate() throws Exception {
        // given
        Long id = 1L;

        // when & then
        mockMvc.perform(patch("/api/v1/categories/{id}/deactivate", id))
                .andDo(print())
                .andExpect(status().isOk());

        verify(categoryService).deactivate(id);
    }

    @DisplayName("카테고리를 활성화한다.")
    @Test
    void activate() throws Exception {
        // given
        Long id = 1L;

        // when & then
        mockMvc.perform(patch("/api/v1/categories/{id}/activate", id))
                .andDo(print())
                .andExpect(status().isOk());

        verify(categoryService).activate(id);
    }

    @DisplayName("카테고리를 삭제한다.")
    @Test
    void remove() throws Exception {
        // given
        Long id = 1L;

        // when & then
        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andDo(print())
                .andExpect(status().isOk());

        verify(categoryService).remove(id);
    }

    @DisplayName("카테고리 상세 정보를 조회한다.")
    @Test
    void detail() throws Exception {
        // given
        Long id = 1L;
        CategoryResponse response = new CategoryResponse(id, "커피", "설명", 1, true, LocalDateTime.now(), LocalDateTime.now());
        given(categoryService.get(id)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("커피"));
    }

    @DisplayName("활성 카테고리 목록을 조회한다.")
    @Test
    void listActive() throws Exception {
        // given
        CategoryResponse response = new CategoryResponse(1L, "커피", "설명", 1, true, LocalDateTime.now(), LocalDateTime.now());
        PageResponse<CategoryResponse> pageResponse = PageResponse.from(new PageImpl<>(List.of(response)));

        given(categoryService.listActive(any(Pageable.class))).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/categories/list/active")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("커피"));
    }

    @DisplayName("전체 카테고리 목록을 조회한다.")
    @Test
    void listByDisplayOrder() throws Exception {
        // given
        CategoryResponse response = new CategoryResponse(1L, "커피", "설명", 1, true, LocalDateTime.now(), LocalDateTime.now());
        PageResponse<CategoryResponse> pageResponse = PageResponse.from(new PageImpl<>(List.of(response)));

        given(categoryService.listByDisplayOrder(any(Pageable.class))).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/categories/list/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("커피"));
    }
}