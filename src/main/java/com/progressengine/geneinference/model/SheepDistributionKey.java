package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;

import java.io.Serializable;
import java.util.Objects;

public class SheepDistributionKey implements Serializable {

    private Integer sheep;
    private Category category;
    private DistributionType distributionType;
    private Grade grade;

    // Required: default constructor
    public SheepDistributionKey() {}

    public SheepDistributionKey(Integer sheepId, Category category, DistributionType distributionType, Grade grade) {
        this.sheep = sheepId;
        this.category = category;
        this.distributionType = distributionType;
        this.grade = grade;
    }

    // Getters and Setters
    public Integer getSheepId() {
        return sheep;
    }

    public void setSheepId(Integer sheepId) {
        this.sheep = sheepId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public DistributionType getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    // equals() and hashCode() are required for composite key

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SheepDistributionKey that)) return false;
        return Objects.equals(sheep, that.sheep)
                && Objects.equals(category, that.category)
                && distributionType == that.distributionType
                && grade == that.grade;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheep, category, distributionType, grade);
    }
}
