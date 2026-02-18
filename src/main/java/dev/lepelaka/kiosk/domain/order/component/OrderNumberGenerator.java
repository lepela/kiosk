package dev.lepelaka.kiosk.domain.order.component;

import dev.lepelaka.kiosk.domain.order.exception.OrderNumberGenerationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "order:sequence:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate() {

        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = KEY_PREFIX + today;

        try {
            // Redis Atomic Increment (1부터 시작)
            Long sequence = redisTemplate.opsForValue().increment(key);

            // 오늘 첫 주문(sequence == 1)이면 만료 시간 설정 (25시간 후 자동 삭제)
            if (sequence != null && sequence == 1) {
                redisTemplate.expire(key, 25, TimeUnit.HOURS);
            }

            // 4자리 숫자로 포맷팅 (0001, 0002, ...)
            return String.format("%s-%04d", today, sequence);

        } catch (DataAccessException e) {
            // Redis 연결 실패, 타임아웃 등 발생 시 Fallback 처리
            log.error("Redis connection failed. Using fallback order number generator.", e);
            try {
                return generateFallback(today);
            } catch (Exception ex) {
                // Fallback 마저 실패하면 커스텀 예외 발생 (주문 실패 처리)
                throw new OrderNumberGenerationFailedException(ex);
            }
        }
    }

    private String generateFallback(String today) {
        // 대체 로직: UUID의 일부를 사용하여 충돌 방지 (예: 20231010-F1a2b3c)
        // 'F' 접두사를 붙여 Fallback으로 생성된 번호임을 식별 가능하게 함
        String uuidPart = UUID.randomUUID().toString().substring(0, 6);
        return String.format("%s-F%s", today, uuidPart);
    }
}