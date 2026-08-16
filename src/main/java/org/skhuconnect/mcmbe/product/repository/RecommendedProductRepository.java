package org.skhuconnect.mcmbe.product.repository;

import org.skhuconnect.mcmbe.game.entity.DesignDirection;
import org.skhuconnect.mcmbe.product.entity.RecommendedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedProductRepository extends JpaRepository<RecommendedProduct, Long> {

    List<RecommendedProduct> findAllByDesignDirectionOrderByDisplayOrderAsc(
            DesignDirection designDirection
    );
}
