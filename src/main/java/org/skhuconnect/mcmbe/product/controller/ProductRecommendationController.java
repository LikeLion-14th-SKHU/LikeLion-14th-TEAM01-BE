package org.skhuconnect.mcmbe.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.skhuconnect.mcmbe.auth.jwt.AuthenticatedMember;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.skhuconnect.mcmbe.product.dto.ProductRecommendationResponse;
import org.skhuconnect.mcmbe.product.service.ProductRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "상품 추천 API",
        description = "게임 완료 사용자의 저장된 디자인 방향을 기준으로 MCM 상품을 추천하는 API"
)
@RestController
@RequestMapping("/detective/products")
public class ProductRecommendationController {

    private final ProductRecommendationService productRecommendationService;

    public ProductRecommendationController(
            ProductRecommendationService productRecommendationService
    ) {
        this.productRecommendationService = productRecommendationService;
    }

    @Operation(
            summary = "게임 완료 후 추천 상품 조회",
            description = """
                    전체 게임을 완료한 로그인 사용자의 저장된 디자인 방향을 서버에서 조회합니다.
                    프론트엔드는 designDirection을 별도로 전달할 필요가 없습니다.
                    해당 디자인 방향에 등록된 상품을 displayOrder 오름차순으로 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 상품 조회 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token이 없거나 유효하지 않음 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "404", description = "해당 디자인 방향의 추천 상품 데이터가 없음 (PRODUCT_RECOMMENDATION_NOT_FOUND)"),
            @ApiResponse(responseCode = "409", description = "게임 진행 정보·디자인 방향이 없거나 게임 미완료 (DESIGN_DIRECTION_REQUIRED, GAME_NOT_COMPLETED)")
    })
    @GetMapping("/recommendation")
    public ResponseEntity<ApiResTemplate<ProductRecommendationResponse>> getRecommendation(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                productRecommendationService.getRecommendation(authenticatedMember.memberId())
        ));
    }
}
