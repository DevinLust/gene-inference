package com.progressengine.geneinference.model;

import com.progressengine.geneinference.exception.ExcessAlleleDiversityException;
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

    @OneToMany(mappedBy = "relationship", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RelationshipJointDistribution> jointDistributions = new ArrayList<>();

    @Transient
    private Map<Category, Map<GradePair, RelationshipJointDistribution>> jointDistributionsByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean jointDistributionsOrganized = false;

    // One-to-many mapping to phenotype frequencies
    @OneToMany(mappedBy = "relationship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelationshipPhenotypeFrequency> phenotypeFrequencies = new ArrayList<>();

    @Transient
    private Map<Category, Map<Grade, RelationshipPhenotypeFrequency>> phenotypeFrequenciesByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean phenotypeFrequenciesOrganized = false;

    public Relationship() {}

    public Relationship(Sheep parent1, Sheep parent2) {
        this.parent1 = parent1;
        this.parent2 = parent2;
    }


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

    // experimental List of RelationshipJointDistribution
    @PostLoad
    private void postLoad() {
        organizeJointDistributions();
        organizePhenotypeFrequencies();
    }


    private void organizeJointDistributions() {
        if (jointDistributions == null) return;

        jointDistributionsByCategory = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            jointDistributionsByCategory.put(category, new HashMap<>());
        }

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
        for (Category category : Category.values()) {
            phenotypeFrequenciesByCategory.put(category, new EnumMap<>(Grade.class));
        }

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


    public Map<Category, Map<Grade, Integer>> getAllPhenotypeFrequencies() {
        if (!phenotypeFrequenciesOrganized) organizePhenotypeFrequencies();

        return this.phenotypeFrequenciesByCategory.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> getPhenotypeFrequencies(e.getKey())));
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


    private void addPhenotypeFrequency(Category category, Grade grade) {
        Map<Grade, RelationshipPhenotypeFrequency> freqMap = createIfAbsentPhenotypeFrequencies(category);
        freqMap.computeIfAbsent(grade, k -> {
            RelationshipPhenotypeFrequency newFreq = new RelationshipPhenotypeFrequency(this, category, k);
            this.phenotypeFrequencies.add(newFreq);
            return newFreq;
        }).addFrequency(1);
    }


    public void addChildToRelationship(Sheep child) {
        Relationship parent = child.getParentRelationship();
        if (parent != null && this.id.equals(parent.getId())) return;

        checkForExcessAlleles(child);
        for (Category category : Category.values()) {
            addPhenotypeFrequency(category, child.getPhenotype(category));
        }

        child.setParentRelationship(this);
    }


    public void removeChildFromRelationship(Sheep child) {
        Relationship parent = child.getParentRelationship();

        if (parent == null || !this.id.equals(parent.getId())) {
            throw new IllegalArgumentException("Sheep is not a child of this relationship");
        }

        for (Category category : Category.values()) {
            Map<Grade, RelationshipPhenotypeFrequency> freqMap = getPhenotypeFrequenciesByCategory(category);
            freqMap.get(child.getPhenotype(category)).removeFrequency(1);
        }

        child.setParentRelationship(null);
    }


    // throws ExcessAlleleDiversityException if adding this child would result in more potential alleles than possible
    private void checkForExcessAlleles(Sheep child) {
        for (Category category : Category.values()) {
            Grade newAllele = child.getPhenotype(category);
            Map<Grade, Integer> phenotypeFrequency = getPhenotypeFrequencies(category);
            // add each parent phenotype to the map so they get counted
            phenotypeFrequency.merge(this.getParent1().getPhenotype(category), 1, Integer::sum);
            phenotypeFrequency.merge(this.getParent2().getPhenotype(category), 1, Integer::sum);
            phenotypeFrequency.merge(newAllele, 1, Integer::sum);

            Set<Grade> nonZeroCounts = EnumSet.noneOf(Grade.class);
            for (Map.Entry<Grade, Integer> entry : phenotypeFrequency.entrySet()) {
                if (entry.getValue() > 0) {
                    nonZeroCounts.add(entry.getKey());
                }
            }
            int maxDistinct = this.getParent1().getPhenotype(category).equals(this.getParent2().getPhenotype(category)) ? 3 : 4;
            if (nonZeroCounts.size() > maxDistinct) {
                nonZeroCounts.remove(newAllele);
                throw new ExcessAlleleDiversityException("Adding this sheep would result in " + (maxDistinct + 1) + " distinct alleles when only " + maxDistinct + " are possible", nonZeroCounts, newAllele, category);
            }
        }
    }
}
