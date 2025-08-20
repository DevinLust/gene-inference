package com.progressengine.geneinference.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

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

//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name = "relationship_hidden_pairs_distribution", joinColumns = @JoinColumn(name = "relationship_id"))
//    @MapKeyClass(GradePair.class)
//    @Column(name = "probability")
//    @JsonDeserialize(keyUsing = GradePairKeyDeserializer.class)
//    private Map<GradePair, Double> hiddenPairsDistribution;

    @OneToMany(mappedBy = "relationship", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RelationshipJointDistribution> jointDistributions = new ArrayList<>();

    @Transient
    private Map<Category, Map<GradePair, RelationshipJointDistribution>> jointDistributionsByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean jointDistributionsOrganized = false;

//    @ElementCollection(fetch = FetchType.EAGER)
//    @MapKeyEnumerated(EnumType.STRING)
//    @MapKeyColumn(name = "grade")         // Name of the key column (for Grade enum)
//    @Column(name = "frequency")           // Name of the value column (Integer)
//    private Map<Grade, Integer> offspringPhenotypeFrequency;

    // One-to-many mapping to phenotype frequencies
    @OneToMany(mappedBy = "relationship", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<RelationshipPhenotypeFrequency> phenotypeFrequencies = new ArrayList<>();

    @Transient
    private Map<Category, Map<Grade, RelationshipPhenotypeFrequency>> phenotypeFrequenciesByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean phenotypeFrequenciesOrganized = false;


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

//    public Map<GradePair, Double> getHiddenPairsDistribution() {
//        return hiddenPairsDistribution;
//    }
//
//    public void setHiddenPairsDistribution(Map<GradePair, Double> hiddenPairsDistribution) {
//        this.hiddenPairsDistribution = hiddenPairsDistribution;
//    }

    // experimental List of RelationshipJointDistribution
    @PostLoad
    private void postLoad() {
        organizeJointDistributions();
        organizePhenotypeFrequencies();
    }

    private void organizeJointDistributions() {
        if (jointDistributions == null) return;

        jointDistributionsByCategory = new EnumMap<>(Category.class);

        for (RelationshipJointDistribution dist : jointDistributions) {
            GradePair gradePairKey = new GradePair(dist.getGrade1(), dist.getGrade2());
            jointDistributionsByCategory
                    .computeIfAbsent(dist.getCategory(), k -> new HashMap<>())
                    .put(gradePairKey, dist);
        }
        jointDistributionsOrganized = true;
    }

    private void organizePhenotypeFrequencies() {
        if (phenotypeFrequencies == null) return;

        phenotypeFrequenciesByCategory = new EnumMap<>(Category.class);

        for (RelationshipPhenotypeFrequency freq : phenotypeFrequencies) {
            phenotypeFrequenciesByCategory
                    .computeIfAbsent(freq.getCategory(), k -> new EnumMap<>(Grade.class))
                    .put(freq.getAllele(), freq);
        }
        phenotypeFrequenciesOrganized = true;
    }

    private Map<GradePair, RelationshipJointDistribution> getDistributionByCategory(Category category) {
        if (!jointDistributionsByCategory.containsKey(category)) {
            throw new IllegalStateException("Distribution not initialized for category " + category);
        }
        return jointDistributionsByCategory.get(category);
    }

    public Map<GradePair, Double> getJointDistribution(Category category) {
        if (!jointDistributionsOrganized) organizeJointDistributions();

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

    @Transactional
    public void setJointDistribution(Category category, Map<GradePair, Double> jointDistribution) {
        if  (!jointDistributionsOrganized) organizeJointDistributions();

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
    @Transactional
    public void setJointDistribution(String categoryStr, Map<GradePair, Double> jointDistribution) {
        setJointDistribution(Category.valueOf(categoryStr), jointDistribution);
    }

//    public Map<Grade, Integer> getOffspringPhenotypeFrequency() {
//        return offspringPhenotypeFrequency;
//    }
//
//    public void setOffspringPhenotypeFrequency(Map<Grade, Integer> offspringPhenotypeFrequency) {
//        this.offspringPhenotypeFrequency = offspringPhenotypeFrequency;
//    }
//
//    public void updateOffspringPhenotypeFrequency(Grade grade, int additionalOccurrences) {
//        this.offspringPhenotypeFrequency.merge(grade, additionalOccurrences, Integer::sum);
//    }

    // experimental List of RelationshipPhenotypeFrequency
    private Map<Grade, RelationshipPhenotypeFrequency> getPhenotypeFrequenciesByCategory(Category category) {
        if (!phenotypeFrequenciesByCategory.containsKey(category)) {
            throw new IllegalStateException("Phenotype frequency for category " + category + " not initialized");
        }
        return phenotypeFrequenciesByCategory.get(category);
    }

    public Map<Grade, Integer> getPhenotypeFrequencies(Category category) {
        if (!phenotypeFrequenciesOrganized) organizePhenotypeFrequencies();

        Map<Grade, RelationshipPhenotypeFrequency> freqMap = getPhenotypeFrequenciesByCategory(category);

        return freqMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFrequency()));
    }
    public Map<Grade, Integer> getPhenotypeFrequencies(String categoryStr) {
        return getPhenotypeFrequencies(Category.valueOf(categoryStr));
    }

    private Map<Grade, RelationshipPhenotypeFrequency> createIfAbsentPhenotypeFrequencies(Category category) {
        return phenotypeFrequenciesByCategory.computeIfAbsent(category, k -> new EnumMap<>(Grade.class));
    }

    @Transactional
    public void setPhenotypeFrequencies(Category category, Map<Grade, Integer> phenotypeFrequencies) {
         if (phenotypeFrequenciesOrganized) organizePhenotypeFrequencies();

        Map<Grade, RelationshipPhenotypeFrequency> freqMap = createIfAbsentPhenotypeFrequencies(category);

        for (Map.Entry<Grade, Integer> entry : phenotypeFrequencies.entrySet()) {
            Grade grade = entry.getKey();
            Integer phenotypeFrequency = entry.getValue();
            RelationshipPhenotypeFrequency freq = freqMap.computeIfAbsent(
                    grade,
                    key -> {
                        RelationshipPhenotypeFrequency newFreq = new RelationshipPhenotypeFrequency(this, category, key);
                        this.phenotypeFrequencies.add(newFreq);
                        return newFreq;
                    }
            );
            freq.setFrequency(phenotypeFrequency);
        }
    }
    @Transactional
    public void setPhenotypeFrequencies(String categoryStr, Map<Grade, Integer> phenotypeFrequencies) {
        setPhenotypeFrequencies(Category.valueOf(categoryStr), phenotypeFrequencies);
    }

    public void updatePhenotypeFrequency(Category category, Grade grade, int additionalOccurrences) {
        Map<Grade, RelationshipPhenotypeFrequency> freqMap = createIfAbsentPhenotypeFrequencies(category);
        freqMap.computeIfAbsent(grade, k -> {
            RelationshipPhenotypeFrequency newFreq = new RelationshipPhenotypeFrequency(this, category, k);
            this.phenotypeFrequencies.add(newFreq);
            return newFreq;
        }).addFrequency(additionalOccurrences);
    }
}
