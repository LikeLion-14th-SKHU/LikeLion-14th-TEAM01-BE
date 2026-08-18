package org.skhuconnect.mcmbe.mypage.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DesignerPassGradeTest {

    @Test
    void selectsGradeAtEveryProbabilityBoundary() {
        assertThat(DesignerPassGrade.fromRandomValue(0)).isEqualTo(DesignerPassGrade.BROWN);
        assertThat(DesignerPassGrade.fromRandomValue(31)).isEqualTo(DesignerPassGrade.BROWN);
        assertThat(DesignerPassGrade.fromRandomValue(32)).isEqualTo(DesignerPassGrade.IVORY);
        assertThat(DesignerPassGrade.fromRandomValue(63)).isEqualTo(DesignerPassGrade.IVORY);
        assertThat(DesignerPassGrade.fromRandomValue(64)).isEqualTo(DesignerPassGrade.NAVY);
        assertThat(DesignerPassGrade.fromRandomValue(95)).isEqualTo(DesignerPassGrade.NAVY);
        assertThat(DesignerPassGrade.fromRandomValue(96)).isEqualTo(DesignerPassGrade.GOLDEN);
        assertThat(DesignerPassGrade.fromRandomValue(99)).isEqualTo(DesignerPassGrade.GOLDEN);
    }
}
