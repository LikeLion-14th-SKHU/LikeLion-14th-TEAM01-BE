package org.skhuconnect.mcmbe.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.product.entity.RecommendedProduct;

public record RecommendedProductResponse(
        @Schema(description = "추천 상품 식별자", example = "1")
        Long id,

        @Schema(description = "MCM 원본 상품명", example = "OTTOMAR 비세토스 위켄더")
        String name,

        @Schema(description = "MCM 원본 상품 이미지 URL")
        String imageUrl,

        @Schema(description = "MCM 원본 상품 상세 페이지 URL")
        String detailUrl,

        @Schema(description = "결과 화면 표시 순서", example = "1")
        int displayOrder
) {
    public static RecommendedProductResponse from(RecommendedProduct product) {
        return new RecommendedProductResponse(
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                product.getDetailUrl(),
                product.getDisplayOrder()
        );
    }
}
