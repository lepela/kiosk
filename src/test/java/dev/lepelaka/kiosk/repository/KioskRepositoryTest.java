package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.entity.Kiosk;
import dev.lepelaka.kiosk.entity.enums.KioskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class KioskRepositoryTest {

    @Autowired
    private KioskRepository kioskRepository;

    @Test
    @DisplayName("키오스크 저장 및 조회")
    void save_and_find() {
        // given
        Kiosk kiosk = Kiosk.builder()
                .location("매장1-1호기")
                .status(KioskStatus.ACTIVE)
                .build();

        // when
        Kiosk saved = kioskRepository.save(kiosk);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLocation()).isEqualTo("매장1-1호기");
        assertThat(saved.getStatus()).isEqualTo(KioskStatus.ACTIVE);
    }

    @Test
    @DisplayName("상태별 키오스크 조회")
    void findByStatus() {
        // given
        Kiosk kiosk1 = createKiosk("매장1-1호기", KioskStatus.ACTIVE);
        Kiosk kiosk2 = createKiosk("매장1-2호기", KioskStatus.ACTIVE);
        Kiosk kiosk3 = createKiosk("매장1-3호기", KioskStatus.MAINTENANCE);

        kioskRepository.saveAll(List.of(kiosk1, kiosk2, kiosk3));

        // when
        List<Kiosk> activeKiosks = kioskRepository.findByStatus(KioskStatus.ACTIVE);

        // then
        assertThat(activeKiosks).hasSize(2);
        assertThat(activeKiosks)
                .extracting("status")
                .containsOnly(KioskStatus.ACTIVE);
    }

    @Test
    @DisplayName("위치로 키오스크 조회")
    void findByLocation() {
        // given
        Kiosk kiosk = createKiosk("매장1-1호기", KioskStatus.ACTIVE);
        kioskRepository.save(kiosk);

        // when
        Optional<Kiosk> found = kioskRepository.findByLocation("매장1-1호기");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getLocation()).isEqualTo("매장1-1호기");
    }

    @Test
    @DisplayName("활성 + 상태별 조회")
    void findByStatusAndActiveTrue() {
        // given
        Kiosk kiosk1 = createKiosk("매장1-1호기", KioskStatus.ACTIVE);
        Kiosk kiosk2 = createKiosk("매장1-2호기", KioskStatus.ACTIVE);

        kioskRepository.saveAll(List.of(kiosk1, kiosk2));

        // when
        List<Kiosk> kiosks = kioskRepository
                .findByStatusAndActiveTrue(KioskStatus.ACTIVE);

        // then
        assertThat(kiosks).hasSize(2);
    }

    // 헬퍼 메서드
    private Kiosk createKiosk(String location, KioskStatus status) {
        return Kiosk.builder()
                .location(location)
                .status(status)
                .build();
    }
}