package org.skhuconnect.mcmbe.product.service;

import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.entity.GameStatus;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.product.dto.ProductRecommendationResponse;
import org.skhuconnect.mcmbe.product.dto.RecommendedProductResponse;
import org.skhuconnect.mcmbe.product.repository.RecommendedProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ProductRecommendationService {

    private final GameProgressRepository gameProgressRepository;
    private final RecommendedProductRepository recommendedProductRepository;

    public ProductRecommendationService(
            GameProgressRepository gameProgressRepository,
            RecommendedProductRepository recommendedProductRepository
    ) {
        this.gameProgressRepository = gameProgressRepository;
        this.recommendedProductRepository = recommendedProductRepository;
    }

    @Transactional(readOnly = true)
    public ProductRecommendationResponse getRecommendation(Long memberId) {
        GameProgress gameProgress = gameProgressRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DESIGN_DIRECTION_REQUIRED));

        if (gameProgress.getStatus() != GameStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.GAME_NOT_COMPLETED);
        }

        DesignDirection designDirection = gameProgress.getDesignDirection();
        if (designDirection == null) {
            throw new BusinessException(ErrorCode.DESIGN_DIRECTION_REQUIRED);
        }

        List<RecommendedProductResponse> products = recommendedProductRepository
                .findAllByDesignDirectionOrderByDisplayOrderAsc(designDirection)
                .stream()
                .sorted(Comparator.comparingInt(product -> product.getDisplayOrder()))
                .map(RecommendedProductResponse::from)
                .toList();
        if (products.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_RECOMMENDATION_NOT_FOUND);
        }

        return new ProductRecommendationResponse(designDirection, products);
    }
}
