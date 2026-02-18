package dev.lepelaka.kiosk.domain.order.component;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "order:sequence:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate() {

        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = KEY_PREFIX + today;

        // Redis Atomic Increment (1부터 시작)
        Long sequence = redisTemplate.opsForValue().increment(key);

        // 오늘 첫 주문(sequence == 1)이면 만료 시간 설정 (25시간 후 자동 삭제)
        if (sequence != null && sequence == 1) {
            redisTemplate.expire(key, 25, TimeUnit.HOURS);
        }

        // 4자리 숫자로 포맷팅 (0001, 0002, ...)
        return String.format("%s-%04d", today, sequence);
    }
}