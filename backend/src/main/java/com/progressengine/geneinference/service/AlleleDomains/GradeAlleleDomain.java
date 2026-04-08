package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.model.enums.Category;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.List;

@Component
public class GradeAlleleDomain implements AlleleDomain<Grade> {
    
    @Override
    public Set<Category> supportedCategories() {
        return EnumSet.of(
                Category.SWIM,
                Category.FLY,
                Category.RUN,
                Category.POWER,
                Category.STAMINA
        );
    }

    @Override
    public Class<Grade> getAlleleType() {
        return Grade.class;
    }

    @Override
    public List<Grade> getAlleles() {
        return List.of(Grade.values());
    }

    @Override
    public Grade parse(String code) {
        return Grade.valueOf(code);
    }
}
