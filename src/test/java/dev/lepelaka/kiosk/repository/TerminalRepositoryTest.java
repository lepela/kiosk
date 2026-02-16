package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class TerminalRepositoryTest {

    @Autowired
    private TerminalRepository terminalRepository;

    @Test
    @DisplayName("키오스크 저장 및 조회")
    void save_and_find() {
        // given
        Terminal terminal = Terminal.builder()
                .location("매장1-1호기")
                .status(TerminalStatus.ACTIVE)
                .build();

        // when
        Terminal saved = terminalRepository.save(terminal);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLocation()).isEqualTo("매장1-1호기");
        assertThat(saved.getStatus()).isEqualTo(TerminalStatus.ACTIVE);
    }

    @Test
    @DisplayName("상태별 키오스크 조회")
    void findByStatus() {
        // given
        Terminal terminal1 = createTerminal("매장1-1호기", TerminalStatus.ACTIVE);
        Terminal terminal2 = createTerminal("매장1-2호기", TerminalStatus.ACTIVE);
        Terminal terminal3 = createTerminal("매장1-3호기", TerminalStatus.MAINTENANCE);

        terminalRepository.saveAll(List.of(terminal1, terminal2, terminal3));

        // when
        List<Terminal> activeTerminals = terminalRepository.findByStatus(TerminalStatus.ACTIVE);

        // then
        assertThat(activeTerminals).hasSize(2);
        assertThat(activeTerminals)
                .extracting("status")
                .containsOnly(TerminalStatus.ACTIVE);
    }

    @Test
    @DisplayName("위치로 키오스크 조회")
    void findByLocation() {
        // given
        Terminal terminal = createTerminal("매장1-1호기", TerminalStatus.ACTIVE);
        terminalRepository.save(terminal);

        // when
        Optional<Terminal> found = terminalRepository.findByLocation("매장1-1호기");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getLocation()).isEqualTo("매장1-1호기");
    }

    @Test
    @DisplayName("활성 + 상태별 조회")
    void findByStatusAndActiveTrue() {
        // given
        Terminal terminal1 = createTerminal("매장1-1호기", TerminalStatus.ACTIVE);
        Terminal terminal2 = createTerminal("매장1-2호기", TerminalStatus.ACTIVE);

        terminalRepository.saveAll(List.of(terminal1, terminal2));

        // when
        List<Terminal> terminals = terminalRepository
                .findByStatusAndActiveTrue(TerminalStatus.ACTIVE);

        // then
        assertThat(terminals).hasSize(2);
    }

    // 헬퍼 메서드
    private Terminal createTerminal(String location, TerminalStatus status) {
        return Terminal.builder()
                .location(location)
                .status(status)
                .build();
    }
}