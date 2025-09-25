package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@IdClass(SheepDistributionKey.class)  // <-- Tells JPA to use the key class
@Table(name = "sheep_distribution")
public class SheepDistribution {

    @Id
    @ManyToOne
    @JoinColumn(name = "sheep_id", referencedColumnName = "id", nullable = false)
    private Sheep sheep;

    @Id
    @Enumerated(EnumType.STRING)
    private Category category;

    @Id
    @Enumerated(EnumType.STRING)
    private DistributionType distributionType;

    @Id
    @Enumerated(EnumType.STRING)
    private Grade grade;

    private double probability;

    public SheepDistribution() {}

    public SheepDistribution(Sheep sheep, Category category, DistributionType distributionType, Grade grade) {
        this.sheep = sheep;
        this.category = category;
        this.distributionType = distributionType;
        this.grade = grade;
    }

    public Sheep getSheep() {
        return sheep;
    }

    public void setSheep(Sheep sheep) {
        this.sheep = sheep;
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

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SheepDistribution that)) return false;
        return Objects.equals(sheep.getId(), that.sheep.getId()) &&
                category == that.category &&
                distributionType == that.distributionType &&
                grade == that.grade;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheep != null ? sheep.getId() : null, category, distributionType, grade);
    }
}
