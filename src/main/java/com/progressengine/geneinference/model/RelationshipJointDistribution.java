package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.persistence.*;

@Entity
@IdClass(RelationshipJointDistributionKey.class)
@Table(name = "relationship_joint_distribution")
public class RelationshipJointDistribution {

    @Id
    @ManyToOne
    @JoinColumn(name = "relationship_id", nullable = false)
    private Relationship relationship;

    @Id
    @Enumerated(EnumType.STRING)
    private Category category;

    @Id
    @Enumerated(EnumType.STRING)
    private Grade grade1;

    @Id
    @Enumerated(EnumType.STRING)
    private Grade grade2;

    private double probability;

    public RelationshipJointDistribution() {}

    public RelationshipJointDistribution(Relationship relationship, Category category, GradePair gradePair) {
        this.relationship = relationship;
        this.category = category;
        this.grade1 = gradePair.getFirst();
        this.grade2 = gradePair.getSecond();
    }

    // getters and setters

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Grade getGrade1() {
        return grade1;
    }

    public void setGrade1(Grade grade1) {
        this.grade1 = grade1;
    }

    public Grade getGrade2() {
        return grade2;
    }

    public void setGrade2(Grade grade2) {
        this.grade2 = grade2;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}

