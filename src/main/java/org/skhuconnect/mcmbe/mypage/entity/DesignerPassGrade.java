package org.skhuconnect.mcmbe.mypage.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;

@Schema(description = "패스 디자인 색상 식별값: BROWN, IVORY, NAVY, GOLDEN")
public enum DesignerPassGrade {

    BROWN("Archive Brown", 0, 31),
    IVORY("Ivory Atelier", 32, 63),
    NAVY("München Navy", 64, 95),
    GOLDEN("Golden 1976", 96, 99);

    private final String displayName;
    private final int minimumValue;
    private final int maximumValue;

    DesignerPassGrade(String displayName, int minimumValue, int maximumValue) {
        this.displayName = displayName;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DesignerPassGrade fromRandomValue(int randomValue) {
        for (DesignerPassGrade grade : values()) {
            if (grade.minimumValue <= randomValue && randomValue <= grade.maximumValue) {
                return grade;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
