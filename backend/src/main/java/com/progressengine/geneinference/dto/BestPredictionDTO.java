package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class BestPredictionDTO {
    private SheepSummaryResponseDTO parent1;
    private SheepSummaryResponseDTO parent2;
    private Map<Category, Grade> parent1BestCategoryGradeMap; // maps category to the grade that won it
    private Map<Category, Grade> parent2BestCategoryGradeMap;
    private Set<Category> bestCategoriesSet;
    private Map<Category, Map<Grade, Double>> phenotypeDistribution; // Maps each category to the distribution a child has these phenotypes

    public BestPredictionDTO(Sheep parent1, Sheep parent2, Map<Category, Grade> parent1BestGrades, Map<Category, Grade> parent2BestGrades, Map<Category, Map<Grade, Double>> predictions) {
        this.parent1 = new SheepSummaryResponseDTO(parent1);
        this.parent2 = new SheepSummaryResponseDTO(parent2);
        this.parent1BestCategoryGradeMap = parent1BestGrades;
        this.parent2BestCategoryGradeMap = parent2BestGrades;
        this.bestCategoriesSet = EnumSet.noneOf(Category.class);
        this.bestCategoriesSet.addAll(parent1BestCategoryGradeMap.keySet());
        this.bestCategoriesSet.addAll(parent2BestCategoryGradeMap.keySet());
        this.phenotypeDistribution = predictions;
    }

    public SheepSummaryResponseDTO getParent1() {
        return parent1;
    }

    public SheepSummaryResponseDTO getParent2() {
        return parent2;
    }

    public Map<Category, Grade> getParent1BestCategoryGradeMap() {
        return parent1BestCategoryGradeMap;
    }

    public Map<Category, Grade> getParent2BestCategoryGradeMap() {
        return parent2BestCategoryGradeMap;
    }

    public Set<Category> getBestCategoriesSet() {
        return bestCategoriesSet;
    }

    public Map<Category, Map<Grade, Double>> getPhenotypeDistribution() {
        return phenotypeDistribution;
    }
}
