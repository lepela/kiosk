package dev.lepelaka.kiosk.domain.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lepelaka.kiosk.domain.category.dto.CategoryCreateRequest;
import dev.lepelaka.kiosk.domain.category.dto.CategoryUpdateRequest;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
    }

    @DisplayName("카테고리 생성부터 조회, 수정까지의 전체 흐름을 검증한다.")
    @Test
    void categoryFullCycle() throws Exception {
        // 1. 카테고리 생성
        CategoryCreateRequest createRequest = new CategoryCreateRequest("통합테스트커피", "설명", 1);
        String createContent = objectMapper.writeValueAsString(createRequest);

        String location = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createContent))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        // Location 헤더에서 ID 추출 (예: http://localhost/api/v1/categories/1)
        String idStr = location.substring(location.lastIndexOf("/") + 1);
        Long id = Long.parseLong(idStr);

        // 2. 상세 조회
        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("통합테스트커피"));

        // 3. 수정
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("수정된커피", "수정된설명", 2);
        mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // 4. 수정 확인 (다시 조회)
        mockMvc.perform(get("/api/v1/categories/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된커피"))
                .andExpect(jsonPath("$.displayOrder").value(2));
        
        // 5. 비활성화
        mockMvc.perform(patch("/api/v1/categories/{id}/deactivate", id))
                .andExpect(status().isOk());
    }
}
