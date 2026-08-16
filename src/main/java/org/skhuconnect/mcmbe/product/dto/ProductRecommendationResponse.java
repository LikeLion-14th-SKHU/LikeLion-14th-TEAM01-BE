package org.skhuconnect.mcmbe.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;

import java.util.List;

public record ProductRecommendationResponse(
        @Schema(
                description = "로그인 사용자가 게임 시작 시 저장한 디자인 방향",
                allowableValues = {"TRAVEL", "DAILY_TRAVEL", "HANDS_FREE"},
                example = "TRAVEL"
        )
        DesignDirection designDirection,

        @Schema(description = "displayOrder 오름차순으로 정렬된 추천 상품")
        List<RecommendedProductResponse> products
) {
}
