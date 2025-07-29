package com.progressengine.geneinference.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.persistence.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "relationship_hidden_pairs_distribution", joinColumns = @JoinColumn(name = "relationship_id"))
    @MapKeyClass(GradePair.class)
    @Column(name = "probability")
    @JsonDeserialize(keyUsing = GradePairKeyDeserializer.class)
    private Map<GradePair, Double> hiddenPairsDistribution;

    @OneToMany(mappedBy = "relationship", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RelationshipJointDistribution> jointDistributions = new ArrayList<>();

    @Transient
    private Map<Category, Map<GradePair, RelationshipJointDistribution>> jointDistributionsByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean organized = false;

    @ElementCollection(fetch = FetchType.EAGER)
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

    // experimental List of RelationshipJointDistribution
    @PostLoad
    private void organizeJointDistributions() {
        if (jointDistributions == null) return;

        jointDistributionsByCategory = new EnumMap<>(Category.class);

        for (RelationshipJointDistribution dist : jointDistributions) {
            GradePair gradePairKey = new GradePair(dist.getGrade1(), dist.getGrade2());
            jointDistributionsByCategory
                    .computeIfAbsent(dist.getCategory(), k -> new HashMap<>())
                    .put(gradePairKey, dist);
        }
        organized = true;
    }

    private Map<GradePair, RelationshipJointDistribution> getDistributionByCategory(Category category) {
        if (!jointDistributionsByCategory.containsKey(category)) {
            throw new IllegalStateException("Distribution not initialized for category " + category);
        }
        return jointDistributionsByCategory.get(category);
    }

    public Map<GradePair, Double> getJointDistribution(Category category) {
        if (!organized) organizeJointDistributions();

        Map<GradePair, RelationshipJointDistribution> jointDistMap = getDistributionByCategory(category);

        return jointDistMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getProbability()));
    }
    public Map<GradePair, Double> getJointDistribution(String categoryStr) {
        return getJointDistribution(Category.valueOf(categoryStr));
    }

    private Map<GradePair, RelationshipJointDistribution> createIfAbsentDistributionByCategory(Category category) {
        return jointDistributionsByCategory
                .computeIfAbsent(category, k -> new HashMap<>());
    }

    public void setJointDistribution(Category category, Map<GradePair, Double> jointDistribution) {
        if  (!organized) organizeJointDistributions();

        int expectedSize = Grade.values().length * Grade.values().length;
        if (jointDistribution.size() != expectedSize) {
            throw new IllegalArgumentException("Joint Distribution does not meet expected size: " + expectedSize + " | Actual size: " + jointDistribution.size());
        }

        // Validate sum ≈ 1.0
        double total = jointDistribution.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        if (Math.abs(total - 1.0) > 1e-6) {
            throw new IllegalArgumentException("Joint distribution probabilities must sum to 1.0 (±1e-6). Actual sum: " + total);
        }

        Map<GradePair, RelationshipJointDistribution> jointDistMap = createIfAbsentDistributionByCategory(category);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePairKey = entry.getKey();
            Double probability = entry.getValue();
            RelationshipJointDistribution dist = jointDistMap.computeIfAbsent(
                    gradePairKey,
                    key -> {
                        RelationshipJointDistribution newDist = new RelationshipJointDistribution(this, category, key);
                        jointDistributions.add(newDist);
                        return newDist;
                    }
            );
            dist.setProbability(probability);
        }
    }
    public void setJointDistributionsByCategory(String categoryStr, Map<GradePair, Double> jointDistribution) {
        setJointDistribution(Category.valueOf(categoryStr), jointDistribution);
    }

    public Map<Grade, Integer> getOffspringPhenotypeFrequency() {
        return offspringPhenotypeFrequency;
    }

    public void setOffspringPhenotypeFrequency(Map<Grade, Integer> offspringPhenotypeFrequency) {
        this.offspringPhenotypeFrequency = offspringPhenotypeFrequency;
    }

    public void updateOffspringPhenotypeFrequency(Grade grade, int additionalOccurrences) {
        this.offspringPhenotypeFrequency.merge(grade, additionalOccurrences, Integer::sum);
    }
}
