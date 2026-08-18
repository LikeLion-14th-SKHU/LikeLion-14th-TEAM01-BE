package org.skhuconnect.mcmbe.mypage.service;

import org.skhuconnect.mcmbe.mypage.entity.DesignerPassGrade;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.IntSupplier;

@Component
public class DesignerPassGradeSelector {

    private final IntSupplier randomValueSupplier;

    public DesignerPassGradeSelector() {
        SecureRandom secureRandom = new SecureRandom();
        this.randomValueSupplier = () -> secureRandom.nextInt(100);
    }

    DesignerPassGradeSelector(IntSupplier randomValueSupplier) {
        this.randomValueSupplier = randomValueSupplier;
    }

    public DesignerPassGrade select() {
        return DesignerPassGrade.fromRandomValue(randomValueSupplier.getAsInt());
    }
}
