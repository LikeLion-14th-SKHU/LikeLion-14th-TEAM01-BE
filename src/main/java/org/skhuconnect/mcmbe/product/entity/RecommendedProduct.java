package org.skhuconnect.mcmbe.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;

@Getter
@Entity
@Table(
        name = "recommended_products",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recommended_products_direction_order",
                columnNames = {"design_direction", "display_order"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "design_direction", nullable = false, length = 20)
    private DesignDirection designDirection;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "detail_url", nullable = false, length = 1000)
    private String detailUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    private RecommendedProduct(
            DesignDirection designDirection,
            String name,
            String imageUrl,
            String detailUrl,
            int displayOrder
    ) {
        this.designDirection = designDirection;
        this.name = name;
        this.imageUrl = imageUrl;
        this.detailUrl = detailUrl;
        this.displayOrder = displayOrder;
    }

    public static RecommendedProduct of(
            DesignDirection designDirection,
            String name,
            String imageUrl,
            String detailUrl,
            int displayOrder
    ) {
        return new RecommendedProduct(
                designDirection,
                name,
                imageUrl,
                detailUrl,
                displayOrder
        );
    }
}
