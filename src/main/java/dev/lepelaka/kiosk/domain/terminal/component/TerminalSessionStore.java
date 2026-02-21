package dev.lepelaka.kiosk.domain.terminal.component;

import dev.lepelaka.kiosk.global.exception.BusinessException;
import dev.lepelaka.kiosk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class TerminalSessionStore {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "term:sess:";
    private static final Duration TTL = Duration.ofHours(12);

    public String createSession(Long terminalId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                PREFIX + token,
                terminalId.toString(),
                TTL
        );
        return token;
    }

    public Long resolveTerminalId(String token) {
        String value = redisTemplate.opsForValue().get(PREFIX + token);
//        if (value == null) throw new BusinessException(TerminalErrorCode.);
        return Long.parseLong(value);
    }

    public void delete(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}