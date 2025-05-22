package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Grade;
import jakarta.persistence.*;

import java.util.List;
import java.util.Map;

@Entity
public class Relationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "parent1_id")
    private Sheep parent1; // foreign key to Sheep

    @ManyToOne
    @JoinColumn(name = "parent2_id")
    private Sheep parent2; // foreign key to Sheep

    @ElementCollection
    @CollectionTable(name = "relationship_hidden_pairs_distribution", joinColumns = @JoinColumn(name = "relationship_id"))
    @MapKeyClass(GradePair.class)
    @Column(name = "probability")
    private Map<GradePair, Double> hiddenPairsDistribution;

    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "grade")         // Name of the key column (for Grade enum)
    @Column(name = "frequency")           // Name of the value column (Integer)
    private Map<Grade, Integer> offspringPhenotypeFrequency;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Sheep getParent1() {
        return parent1;
    }

    public void setParent1(Sheep parent1) {
        this.parent1 = parent1;
    }

    public Sheep getParent2() {
        return parent2;
    }

    public void setParent2(Sheep parent2) {
        this.parent2 = parent2;
    }

    public Map<GradePair, Double> getHiddenPairsDistribution() {
        return hiddenPairsDistribution;
    }

    public void setHiddenPairsDistribution(Map<GradePair, Double> hiddenPairsDistribution) {
        this.hiddenPairsDistribution = hiddenPairsDistribution;
    }

    public Map<Grade, Integer> getOffspringPhenotypeFrequency() {
        return offspringPhenotypeFrequency;
    }

    public void setOffspringPhenotypeFrequency(Map<Grade, Integer> offspringPhenotypeFrequency) {
        this.offspringPhenotypeFrequency = offspringPhenotypeFrequency;
    }
}
