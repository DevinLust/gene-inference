package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;

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
    @Column(name = "allele_code")
    private String alleleCode;

    private double probability;

    public SheepDistribution() {}

    public SheepDistribution(Sheep sheep, Category category, DistributionType distributionType, String alleleCode) {
        this.sheep = sheep;
        this.category = category;
        this.distributionType = distributionType;
        this.alleleCode = alleleCode;
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

    public String getAlleleCode() {
        return this.alleleCode;
    }

    public void setAlleleCode(String alleleCode) {
        this.alleleCode = alleleCode;
    }

    public <A extends Enum<A> & Allele> A getAllele(AlleleDomain<A> domain) {
        return domain.parse(alleleCode);
    }

    public <A extends Enum<A> & Allele> void setAllele(A allele) {
    if (allele == null) {
        throw new IllegalArgumentException("Allele cannot be null");
    }
    this.alleleCode = allele.code();
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
                Objects.equals(alleleCode, that.alleleCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheep != null ? sheep.getId() : null, category, distributionType, alleleCode);
    }
}
