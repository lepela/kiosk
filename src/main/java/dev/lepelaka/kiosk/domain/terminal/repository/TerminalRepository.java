package dev.lepelaka.kiosk.domain.terminal.repository;

import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TerminalRepository extends JpaRepository<Terminal, Long> {
    List<Terminal> findByStatus(TerminalStatus status);

    List<Terminal> findByStatusAndActiveTrue(TerminalStatus status);

    Optional<Terminal> findByLocation(String location);
}
