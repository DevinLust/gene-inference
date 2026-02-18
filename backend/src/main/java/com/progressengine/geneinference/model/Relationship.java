package com.progressengine.geneinference.model;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.exception.ExcessAlleleDiversityException;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.InferenceMath;
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
    @Deprecated
    private List<RelationshipJointDistribution> jointDistributions = new ArrayList<>();

    @Transient
    private Map<Category, Map<GradePair, RelationshipJointDistribution>> jointDistributionsByCategory = new EnumMap<>(Category.class);

    @Transient
    private boolean jointDistributionsOrganized = false;

    // One-to-many mapping to phenotype frequencies
    @OneToMany(mappedBy = "relationship", cascade = CascadeType.ALL, orphanRemoval = true)
    @Deprecated
    private List<RelationshipPhenotypeFrequency> phenotypeFrequencies = new ArrayList<>();

    @Transient
    private Map<Category, Map<Grade, RelationshipPhenotypeFrequency>> phenotypeFrequenciesByCategory = new EnumMap<>(Category.class);

    @Transient
    private Map<Category, Map<GradePair, Double>> jointDistributionCache;

    @Transient
    private boolean jointCacheDirty = true;

    @Transient
    // Stores by category, then parent phenotypes at time of birth, and then phenotype frequency
    private Map<Category, Map<GradePair, Map<Grade, Integer>>> phenotypeFrequencyCache;

    @Transient
    private boolean frequencyCacheDirty = true;

    @Transient
    private boolean phenotypeFrequenciesOrganized = false;

    @OneToMany(mappedBy = "parentRelationship", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<BirthRecord> birthRecords = new ArrayList<>();


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

    public List<BirthRecord> getBirthRecords() {
        return birthRecords;
    }

    // Experimental BrithRecords
    private BirthRecord recordBirth(Sheep child) {
        BirthRecord birthRecord = BirthRecord.create(this, child.getGenotypes(), child);
        birthRecords.add(birthRecord);
        frequencyCacheDirty = true;
        jointCacheDirty = true;
        return birthRecord;
    }

    private BirthRecord recordBirth(Map<Category, SheepGenotypeDTO> childGenotypes) {
        BirthRecord birthRecord = BirthRecord.create(this, childGenotypes, null);
        birthRecords.add(birthRecord);
        frequencyCacheDirty = true;
        jointCacheDirty = true;
        return birthRecord;
    }

    public Map<Category, Map<GradePair, Double>> getJointDistributionsExperimental() {
        checkDirtyJointCache();

        return copyJointCache();
    }

    private void checkDirtyJointCache() {
        if (jointCacheDirty || jointDistributionCache == null) {
            jointDistributionCache = computeJointCache();
            jointCacheDirty = false;
        }
    }

    private Map<Category, Map<GradePair, Double>> computeJointCache() {
        Map<Category, Map<GradePair, Double>> result = new EnumMap<>(Category.class);
        Map<Category, Map<GradePair, Map<Grade, Integer>>> phenotypeRecordFrequencies = getFrequenciesExperimental();

        for (Category category : Category.values()) {
            Map<GradePair, Double> jointEpoch = uniformJointDistribution();
            Map<GradePair, Map<Grade, Integer>> epochFrequencies = phenotypeRecordFrequencies.get(category);
            if (epochFrequencies == null) { throw new IllegalStateException("phenotype frequencies for Category " + category + " not found"); }

            for (Map.Entry<GradePair, Map<Grade, Integer>> entry : epochFrequencies.entrySet()) {
                GradePair pair = entry.getKey();
                Map<Grade, Integer> phenotypeFreq = entry.getValue();
                Map<GradePair, Double> currentJoint = InferenceMath.multinomialJointScores(pair.getFirst(), pair.getSecond(), phenotypeFreq);
                InferenceMath.productOfExperts(jointEpoch, currentJoint);
            }
            result.put(category, jointEpoch);
        }
        return result;
    }

    private Map<GradePair, Double> uniformJointDistribution() {
        Map<GradePair, Double> result = new HashMap<>();
        for (Grade g1 : Grade.values()) {
            for (Grade g2 : Grade.values()) {
                result.put(new GradePair(g1, g2), 1.0);
            }
        }
        InferenceMath.normalizeScores(result);
        return result;
    }

    private Map<Category, Map<GradePair, Double>> copyJointCache() {
        checkDirtyJointCache();

        Map<Category, Map<GradePair, Double>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            Map<GradePair, Double> joint = jointDistributionCache.get(category);
            for (Map.Entry<GradePair, Double> entry : joint.entrySet()) {
                result.computeIfAbsent(category, k -> new HashMap<>())
                        .put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public Map<Category, Map<GradePair, Map<Grade, Integer>>> getFrequenciesExperimental() {
        checkDirtyFrequencyCache();

        return copyFrequencyCache();
    }

    private void checkDirtyFrequencyCache() {
        if (frequencyCacheDirty || phenotypeFrequencyCache == null) {
            phenotypeFrequencyCache = aggregateFrequenciesFromBirthRecords();
            frequencyCacheDirty = false;
        }
    }

    private Map<Category, Map<GradePair, Map<Grade, Integer>>> aggregateFrequenciesFromBirthRecords() {
        Map<Category, Map<GradePair, Map<Grade, Integer>>> result = new EnumMap<>(Category.class);

        for (BirthRecord br : birthRecords) {
            for (BirthRecordPhenotype p : br.getPhenotypesAtBirth()) { // entity list
                Category cat = p.getCategory();
                GradePair parents = new GradePair(p.getParent1Phenotype(), p.getParent2Phenotype());
                Grade child = p.getChildPhenotype();

                result.computeIfAbsent(cat, c -> new HashMap<>())
                        .computeIfAbsent(parents, gp -> new EnumMap<>(Grade.class))
                        .merge(child, 1, Integer::sum);
            }
        }
        return result;
    }

    private Map<Category, Map<GradePair, Map<Grade, Integer>>> copyFrequencyCache() {
        checkDirtyFrequencyCache();

        Map<Category, Map<GradePair, Map<Grade, Integer>>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, new HashMap<>());
            Map<GradePair, Map<Grade, Integer>> categoryFreq = phenotypeFrequencyCache.getOrDefault(category, new HashMap<>());
            for (Map.Entry<GradePair, Map<Grade, Integer>> epoch : categoryFreq.entrySet()) {
                for (Map.Entry<Grade, Integer> phenotypeFreq : epoch.getValue().entrySet()) {
                    result.computeIfAbsent(category, k -> new HashMap<>())
                            .computeIfAbsent(epoch.getKey(), g -> new EnumMap<>(Grade.class))
                            .put(phenotypeFreq.getKey(), phenotypeFreq.getValue());
                }
            }
        }
        return result;
    }

    private void invalidateCaches() {
        jointCacheDirty = true;
        frequencyCacheDirty = true;
    }

    public void removeBirthRecord(BirthRecord br) {
        birthRecords.remove(br);
        br.setParentRelationship(null); // helps orphanRemoval trigger reliably
        invalidateCaches();
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


    public BirthRecord addChildToRelationship(Sheep child) {
        Relationship parent = child.getBirthRecord() != null ? child.getBirthRecord().getParentRelationship() : null;
        if (this.equals(parent)) {
            return child.getBirthRecord();
        } else if (parent != null) {
            throw new IllegalStateException("This child already belongs to a different relationship");
        }

        checkForExcessAllelesExperimental(child.getGenotypes());
        invalidateCaches();
        return recordBirth(child);
    }


    public BirthRecord addChildInformationToRelationship(Sheep child) {
        if (child.getBirthRecord() != null) {
            throw new IllegalStateException("This child already belongs to a relationship");
        }

        checkForExcessAllelesExperimental(child.getGenotypes());
        invalidateCaches();
        return recordBirth(child.getGenotypes());
    }


    public void removeChildFromRelationship(Sheep child) {
        BirthRecord birthRecord = child.getBirthRecord();

        if (birthRecord == null || !this.equals(birthRecord.getParentRelationship())) {
            throw new IllegalStateException("Sheep is not a child of this relationship");
        }

        birthRecords.remove(birthRecord);
        child.setBirthRecord(null);
        birthRecord.setChild(null);
        birthRecord.setParentRelationship(null);
        invalidateCaches();
    }


    // TODO - update for birthRecords when constraints are figured out
    public void updateChildPhenotypeFrequencies(Sheep child, Map<Category, SheepGenotypeDTO> updatedGenotypes) {
        if (!this.id.equals(child.getParentRelationship().getId())) throw new IllegalArgumentException("Sheep is not a child of this relationship");
        if (updatedGenotypes == null || updatedGenotypes.isEmpty()) return;

        Map<Category, GradePair> phenotypeDeltas = new EnumMap<>(Category.class);
        for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
            Category category = entry.getKey();
            GradePair genotype = entry.getValue().toGradePair();

            Grade currentPhenotype = child.getPhenotype(category);
            Grade newPhenotype = genotype.getFirst();
            phenotypeDeltas.compute(category, (k, v) -> currentPhenotype != newPhenotype ? new GradePair(currentPhenotype, newPhenotype) : null);
        }

        checkForExcessAlleles(phenotypeDeltas);

        for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
            Category category = entry.getKey();
            GradePair genotype = entry.getValue().toGradePair();
            child.setGenotype(category, genotype);
        }
        for (Map.Entry<Category, GradePair> entry : phenotypeDeltas.entrySet()) {
            Map<Grade, RelationshipPhenotypeFrequency> freqMap = getPhenotypeFrequenciesByCategory(entry.getKey());
            GradePair delta = entry.getValue();
            freqMap.get(delta.getFirst()).removeFrequency(1);
            freqMap.get(delta.getSecond()).addFrequency(1);
        }
    }


    private void checkForExcessAllelesExperimental(Map<Category, SheepGenotypeDTO> childGenotypes) {
        Map<Category, Map<GradePair, Map<Grade, Integer>>> frequencyCacheCopy = copyFrequencyCache();
        for (Category category : Category.values()) {
            Grade newAllele = childGenotypes.get(category).phenotype();
            GradePair currentParentPhenotypes = new GradePair(parent1.getPhenotype(category), parent2.getPhenotype(category));
            if (newAllele == currentParentPhenotypes.getFirst() || newAllele == currentParentPhenotypes.getSecond()) {
                continue;
            }
            // check all epochs of the current category
            Map<GradePair, Map<Grade, Integer>> epochMap = frequencyCacheCopy.get(category);
            Set<Grade> previousHidden = hiddenAlleleDiversity(epochMap);
            if (previousHidden.size() == 2 && !previousHidden.contains(newAllele)) {
                Set<Grade> validAlleles = EnumSet.of(currentParentPhenotypes.getFirst(), currentParentPhenotypes.getSecond());
                validAlleles.addAll(previousHidden);
                throw new ExcessAlleleDiversityException("Adding this allele would result in excessive allele diversity", validAlleles, newAllele, category);
            }
        }
    }

    private Set<Grade> hiddenAlleleDiversity(Map<GradePair, Map<Grade, Integer>> epochMap) {
        if (epochMap == null || epochMap.isEmpty()) return Collections.emptySet();

        Set<Grade> definitiveHiddenAlleles = EnumSet.noneOf(Grade.class);
        for (Map.Entry<GradePair, Map<Grade, Integer>> entry : epochMap.entrySet()) {
            GradePair parentPhenotypes = entry.getKey();
            Map<Grade, Integer> phenotypesSeen = entry.getValue();
            for (Map.Entry<Grade, Integer> freqPair : phenotypesSeen.entrySet()) {
                Grade phenotype = freqPair.getKey();
                Integer freq = freqPair.getValue();
                if (phenotype != parentPhenotypes.getFirst() && phenotype != parentPhenotypes.getSecond() && freq != null && freq > 0) {
                    definitiveHiddenAlleles.add(phenotype);
                }
            }
        }
        return definitiveHiddenAlleles;
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

            validatePhenotypeFrequency(category, phenotypeFrequency, newAllele);
        }
    }

    // throws ExcessAlleleDiversityException if changing these phenotypes would result in excessive alleles
    private void checkForExcessAlleles(Map<Category, GradePair> phenotypeDeltas) {
        for (Map.Entry<Category, GradePair> entry : phenotypeDeltas.entrySet()) {
            Category category = entry.getKey();
            Grade oldAllele = entry.getValue().getFirst();
            Grade newAllele = entry.getValue().getSecond();

            Map<Grade, Integer> phenotypeFrequency = getPhenotypeFrequencies(category);
            phenotypeFrequency.merge(oldAllele, -1, Integer::sum);
            phenotypeFrequency.merge(newAllele, 1, Integer::sum);

            validatePhenotypeFrequency(category, phenotypeFrequency, newAllele);
        }

    }

    private void validatePhenotypeFrequency(Category category, Map<Grade, Integer> phenotypeFrequency, Grade newAllele) {
        Set<Grade> nonZeroCounts = EnumSet.noneOf(Grade.class);
        for (Map.Entry<Grade, Integer> entry : phenotypeFrequency.entrySet()) {
            if (entry.getValue() > 0) {
                nonZeroCounts.add(entry.getKey());
            }
            if (entry.getValue() < 0) {
                throw new IllegalStateException("Phenotype frequency for grade " + entry.getKey() + " would be negative in " + category);
            }
        }
        int maxDistinct = this.getParent1().getPhenotype(category).equals(this.getParent2().getPhenotype(category)) ? 3 : 4;
        if (nonZeroCounts.size() > maxDistinct) {
            nonZeroCounts.remove(newAllele);
            throw new ExcessAlleleDiversityException("This operation would result in " + (maxDistinct + 1) + " distinct alleles when only " + maxDistinct + " are possible", nonZeroCounts, newAllele, category);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relationship other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
