package dev.lepelaka.kiosk.domain.category.entity;

import dev.lepelaka.kiosk.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@DynamicUpdate
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private int displayOrder;

    @Builder
    public Category(String name, String description, int displayOrder) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public void update(String name, String description, int displayOrder) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }
}
