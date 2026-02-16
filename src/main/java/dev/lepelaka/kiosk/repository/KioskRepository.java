package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.entity.Kiosk;
import dev.lepelaka.kiosk.entity.enums.KioskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KioskRepository extends JpaRepository<Kiosk, Long> {
    List<Kiosk> findByStatus(KioskStatus status);

    List<Kiosk> findByStatusAndActiveTrue(KioskStatus status);

    Optional<Kiosk> findByLocation(String location);
}
