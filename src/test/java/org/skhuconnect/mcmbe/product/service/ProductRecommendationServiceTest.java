package org.skhuconnect.mcmbe.product.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.game.entity.CaseType;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.product.dto.ProductRecommendationResponse;
import org.skhuconnect.mcmbe.product.entity.RecommendedProduct;
import org.skhuconnect.mcmbe.product.repository.RecommendedProductRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductRecommendationServiceTest {

    private final GameProgressRepository gameProgressRepository =
            mock(GameProgressRepository.class);
    private final RecommendedProductRepository productRepository =
            mock(RecommendedProductRepository.class);
    private final ProductRecommendationService service = new ProductRecommendationService(
            gameProgressRepository,
            productRepository
    );

    @ParameterizedTest
    @MethodSource("recommendationsByDesignDirection")
    void returnsOnlyProductsForAuthenticatedMembersDesignDirection(
            DesignDirection designDirection,
            List<String> expectedNames
    ) {
        GameProgress completedGame = completedGame(1L, designDirection);
        List<RecommendedProduct> products = products(designDirection, expectedNames);
        when(gameProgressRepository.findByMemberId(1L))
                .thenReturn(Optional.of(completedGame));
        when(productRepository.findAllByDesignDirectionOrderByDisplayOrderAsc(designDirection))
                .thenReturn(products);

        ProductRecommendationResponse response = service.getRecommendation(1L);

        assertThat(response.designDirection()).isEqualTo(designDirection);
        assertThat(response.products())
                .extracting(product -> product.name())
                .containsExactlyElementsOf(expectedNames);
        assertThat(response.products())
                .extracting(product -> product.displayOrder())
                .containsExactly(1, 2, 3);
        verify(productRepository)
                .findAllByDesignDirectionOrderByDisplayOrderAsc(designDirection);
    }

    @Test
    void blocksRecommendationBeforeGameCompletion() {
        Member member = member(1L);
        GameProgress gameProgress = GameProgress.selectDesign(member, DesignDirection.TRAVEL);
        when(gameProgressRepository.findByMemberId(1L))
                .thenReturn(Optional.of(gameProgress));

        assertError(() -> service.getRecommendation(1L), ErrorCode.GAME_NOT_COMPLETED);
        verify(productRepository, never())
                .findAllByDesignDirectionOrderByDisplayOrderAsc(DesignDirection.TRAVEL);
    }

    @Test
    void blocksRecommendationWhileGameIsInProgress() {
        Member member = member(1L);
        GameProgress gameProgress = GameProgress.selectDesign(member, DesignDirection.TRAVEL);
        gameProgress.selectCase(CaseType.FUNCTION);
        when(gameProgressRepository.findByMemberId(1L))
                .thenReturn(Optional.of(gameProgress));

        assertError(() -> service.getRecommendation(1L), ErrorCode.GAME_NOT_COMPLETED);
        verify(productRepository, never())
                .findAllByDesignDirectionOrderByDisplayOrderAsc(DesignDirection.TRAVEL);
    }

    @Test
    void preservesStoredProductFieldsInResponse() {
        GameProgress completedGame = completedGame(1L, DesignDirection.TRAVEL);
        RecommendedProduct product = RecommendedProduct.of(
                DesignDirection.TRAVEL,
                "OTTOMAR 비세토스 위켄더",
                "https://images.example.test/ottomar.jpg",
                "https://shop.example.test/ottomar",
                1
        );
        ReflectionTestUtils.setField(product, "id", 101L);
        when(gameProgressRepository.findByMemberId(1L))
                .thenReturn(Optional.of(completedGame));
        when(productRepository.findAllByDesignDirectionOrderByDisplayOrderAsc(
                DesignDirection.TRAVEL
        )).thenReturn(List.of(product));

        ProductRecommendationResponse response = service.getRecommendation(1L);

        assertThat(response.products()).singleElement().satisfies(result -> {
            assertThat(result.id()).isEqualTo(101L);
            assertThat(result.name()).isEqualTo("OTTOMAR 비세토스 위켄더");
            assertThat(result.imageUrl()).isEqualTo("https://images.example.test/ottomar.jpg");
            assertThat(result.detailUrl()).isEqualTo("https://shop.example.test/ottomar");
            assertThat(result.displayOrder()).isEqualTo(1);
        });
    }

    @Test
    void blocksRecommendationWhenGameProgressDoesNotExist() {
        when(gameProgressRepository.findByMemberId(1L)).thenReturn(Optional.empty());

        assertError(
                () -> service.getRecommendation(1L),
                ErrorCode.DESIGN_DIRECTION_REQUIRED
        );
    }

    @Test
    void blocksRecommendationWhenDesignDirectionIsMissing() {
        GameProgress completedGame = completedGame(1L, DesignDirection.TRAVEL);
        ReflectionTestUtils.setField(completedGame, "designDirection", null);
        when(gameProgressRepository.findByMemberId(1L))
                .thenReturn(Optional.of(completedGame));

        assertError(
                () -> service.getRecommendation(1L),
                ErrorCode.DESIGN_DIRECTION_REQUIRED
        );
    }

    @Test
    void doesNotUseAnotherMembersDesignDirection() {
        GameProgress authenticatedMembersGame = completedGame(
                1L,
                DesignDirection.HANDS_FREE
        );
        List<String> handsFreeNames = List.of(
                "AREN 비세토스 크로스바디",
                "TRACY 비세토스 숄더백",
                "PINA 스터드 장식 카프스킨 드로우스트링 백"
        );
        when(gameProgressRepository.findByMemberId(1L))
                .thenReturn(Optional.of(authenticatedMembersGame));
        when(productRepository.findAllByDesignDirectionOrderByDisplayOrderAsc(
                DesignDirection.HANDS_FREE
        )).thenReturn(products(DesignDirection.HANDS_FREE, handsFreeNames));

        ProductRecommendationResponse response = service.getRecommendation(1L);

        assertThat(response.designDirection()).isEqualTo(DesignDirection.HANDS_FREE);
        verify(gameProgressRepository).findByMemberId(1L);
        verify(gameProgressRepository, never()).findByMemberId(2L);
        verify(productRepository, never())
                .findAllByDesignDirectionOrderByDisplayOrderAsc(DesignDirection.TRAVEL);
        verify(productRepository, never())
                .findAllByDesignDirectionOrderByDisplayOrderAsc(DesignDirection.DAILY_TRAVEL);
    }

    @Test
    void reportsMissingRecommendationData() {
        GameProgress completedGame = completedGame(1L, DesignDirection.TRAVEL);
        when(gameProgressRepository.findByMemberId(1L))
                .thenReturn(Optional.of(completedGame));
        when(productRepository.findAllByDesignDirectionOrderByDisplayOrderAsc(
                DesignDirection.TRAVEL
        )).thenReturn(List.of());

        assertError(
                () -> service.getRecommendation(1L),
                ErrorCode.PRODUCT_RECOMMENDATION_NOT_FOUND
        );
    }

    private static Stream<Arguments> recommendationsByDesignDirection() {
        return Stream.of(
                Arguments.of(DesignDirection.TRAVEL, List.of(
                        "OTTOMAR 비세토스 위켄더",
                        "비세토스 소재의 오토마 위켄더 백",
                        "AREN 다이아몬드 퀼팅 레더 백팩"
                )),
                Arguments.of(DesignDirection.DAILY_TRAVEL, List.of(
                        "LENI 비세토스 쇼퍼",
                        "AREN 비세토스 호보",
                        "AREN 비세토스 E/W 숄더백"
                )),
                Arguments.of(DesignDirection.HANDS_FREE, List.of(
                        "AREN 비세토스 크로스바디",
                        "TRACY 비세토스 숄더백",
                        "PINA 스터드 장식 카프스킨 드로우스트링 백"
                ))
        );
    }

    private List<RecommendedProduct> products(
            DesignDirection designDirection,
            List<String> names
    ) {
        RecommendedProduct third = product(designDirection, names.get(2), 3, 103L);
        RecommendedProduct first = product(designDirection, names.get(0), 1, 101L);
        RecommendedProduct second = product(designDirection, names.get(1), 2, 102L);
        return List.of(third, first, second);
    }

    private RecommendedProduct product(
            DesignDirection designDirection,
            String name,
            int displayOrder,
            Long id
    ) {
        RecommendedProduct product = RecommendedProduct.of(
                designDirection,
                name,
                "https://images.example.test/product.jpg",
                "https://shop.example.test/product",
                displayOrder
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private GameProgress completedGame(Long memberId, DesignDirection designDirection) {
        GameProgress gameProgress = GameProgress.selectDesign(
                member(memberId),
                designDirection
        );
        ReflectionTestUtils.setField(gameProgress, "id", memberId * 10);
        gameProgress.selectCase(CaseType.FUNCTION);
        gameProgress.completeCurrentCase(true);
        gameProgress.selectCase(CaseType.SIGNATURE);
        gameProgress.completeCurrentCase(true);
        return gameProgress;
    }

    private Member member(Long memberId) {
        Member member = Member.kakao(
                "provider-" + memberId,
                null,
                "회원",
                null
        );
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
