package com.progressengine.geneinference.model;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.exception.ExcessAlleleDiversityException;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

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

    @Transient
    private Map<Category, Map<AlleleCodePair, Double>> jointDistributionCache;

    @Transient
    private boolean jointCacheDirty = true;

    @Transient
    // Stores by category, then parent phenotypes at time of birth, and then phenotype frequency
    private Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeFrequencyCache;

    @Transient
    private boolean frequencyCacheDirty = true;

    @OneToMany(mappedBy = "parentRelationship", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<BirthRecord> birthRecords = new HashSet<>();


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


    public Set<BirthRecord> getBirthRecords() {
        return Collections.unmodifiableSet(birthRecords);
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


    public Map<Category, Map<AlleleCodePair, Double>> getJointDistributions() {
        checkDirtyJointCache();

        return copyJointCache();
    }


    private void checkDirtyJointCache() {
        if (jointCacheDirty || jointDistributionCache == null) {
            jointDistributionCache = computeJointCache();
            jointCacheDirty = false;
        }
    }


    private Map<Category, Map<AlleleCodePair, Double>> computeJointCache() {
        Map<Category, Map<AlleleCodePair, Double>> result = new EnumMap<>(Category.class);
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeRecordFrequencies = copyFrequencyCache();

        for (Category category : Category.values()) {
            Map<AlleleCodePair, Double> jointEpoch = uniformJointDistribution(category);
            Map<AlleleCodePair, Map<String, Integer>> epochFrequencies = phenotypeRecordFrequencies.get(category);

            if (epochFrequencies == null) {
                throw new IllegalStateException("phenotype frequencies for Category " + category + " not found");
            }

            computeJointCacheForCategory(category, jointEpoch, epochFrequencies);
            result.put(category, jointEpoch);
        }

        return result;
    }

    private <A extends Enum<A> & Allele> void computeJointCacheForCategory(
        Category category,
        Map<AlleleCodePair, Double> jointEpoch,
        Map<AlleleCodePair, Map<String, Integer>> epochFrequencies
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        for (Map.Entry<AlleleCodePair, Map<String, Integer>> entry : epochFrequencies.entrySet()) {
            AlleleCodePair pair = entry.getKey();
            Map<String, Integer> phenotypeFreq = entry.getValue();

            A parent1 = domain.parse(pair.first());
            A parent2 = domain.parse(pair.second());

            Map<A, Integer> typedPhenotypeFreq = new EnumMap<>(domain.getAlleleType());
            for (Map.Entry<String, Integer> freqEntry : phenotypeFreq.entrySet()) {
                A phenotype = domain.parse(freqEntry.getKey());
                typedPhenotypeFreq.put(phenotype, freqEntry.getValue());
            }

            Map<AllelePair<A>, Double> currentJoint =
                    InferenceMath.multinomialJointScores(parent1, parent2, typedPhenotypeFreq, domain);

            Map<AlleleCodePair, Double> currentJointCodes = new HashMap<>();
            for (Map.Entry<AllelePair<A>, Double> jointEntry : currentJoint.entrySet()) {
                currentJointCodes.put(
                        AlleleCodePair.fromAllelePair(jointEntry.getKey()),
                        jointEntry.getValue()
                );
            }

            InferenceMath.productOfExperts(jointEpoch, currentJointCodes);
        }
    }


    private Map<AlleleCodePair, Double> uniformJointDistribution(Category category) {
        AlleleDomain<?> domain = CategoryDomains.domainFor(category);

        Map<AlleleCodePair, Double> result = new HashMap<>();

        for (Allele a1 : domain.getAlleles()) {
            for (Allele a2 : domain.getAlleles()) {
                result.put(
                        new AlleleCodePair(a1.code(), a2.code()),
                        1.0
                );
            }
        }

        InferenceMath.normalizeScores(result);
        return result;
    }


    private Map<Category, Map<AlleleCodePair, Double>> copyJointCache() {
        checkDirtyJointCache();

        Map<Category, Map<AlleleCodePair, Double>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            Map<AlleleCodePair, Double> joint = jointDistributionCache.get(category);
            for (Map.Entry<AlleleCodePair, Double> entry : joint.entrySet()) {
                result.computeIfAbsent(category, k -> new HashMap<>())
                        .put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }


    public Map<Category, Map<AlleleCodePair, Map<String, Integer>>> getPhenotypeFrequencies() {
        checkDirtyFrequencyCache();

        return copyFrequencyCache();
    }


    public <A extends Enum<A> & Allele> Map<AllelePair<A>, Map<A, Integer>> getPhenotypeFrequencies(Category category) {
        checkDirtyFrequencyCache();

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<AlleleCodePair, Map<String, Integer>> raw =
                phenotypeFrequencyCache.getOrDefault(category, Map.of());

        Map<AllelePair<A>, Map<A, Integer>> result = new HashMap<>();

        for (Map.Entry<AlleleCodePair, Map<String, Integer>> outer : raw.entrySet()) {
            AllelePair<A> parentPair = outer.getKey().toAllelePair(domain);

            Map<A, Integer> childCounts = new EnumMap<>(domain.getAlleleType());
            for (Map.Entry<String, Integer> inner : outer.getValue().entrySet()) {
                childCounts.put(domain.parse(inner.getKey()), inner.getValue());
            }

            result.put(parentPair, childCounts);
        }

        return result;
    }


    private void checkDirtyFrequencyCache() {
        if (frequencyCacheDirty || phenotypeFrequencyCache == null) {
            phenotypeFrequencyCache = aggregateFrequenciesFromBirthRecords();
            frequencyCacheDirty = false;
        }
    }


    private Map<Category, Map<AlleleCodePair, Map<String, Integer>>> aggregateFrequenciesFromBirthRecords() {
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> result = new EnumMap<>(Category.class);

        for (BirthRecord br : birthRecords) {
            for (BirthRecordPhenotype p : br.getPhenotypesAtBirth()) {
                Category category = p.getCategory();

                AlleleCodePair parents = new AlleleCodePair(
                        p.getParent1PhenotypeCode(),
                        p.getParent2PhenotypeCode()
                );

                String childPhenotypeCode = p.getChildPhenotypeCode();

                result.computeIfAbsent(category, c -> new HashMap<>())
                        .computeIfAbsent(parents, k -> new HashMap<>())
                        .merge(childPhenotypeCode, 1, Integer::sum);
            }
        }

        return result;
    }


    private Map<Category, Map<AlleleCodePair, Map<String, Integer>>> copyFrequencyCache() {
        checkDirtyFrequencyCache();

        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, new HashMap<>());
            Map<AlleleCodePair, Map<String, Integer>> categoryFreq = phenotypeFrequencyCache.getOrDefault(category, new HashMap<>());
            for (Map.Entry<AlleleCodePair, Map<String, Integer>> epoch : categoryFreq.entrySet()) {
                for (Map.Entry<String, Integer> phenotypeFreq : epoch.getValue().entrySet()) {
                    result.computeIfAbsent(category, k -> new HashMap<>())
                            .computeIfAbsent(epoch.getKey(), g -> new HashMap<>())
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


    public void addBirthRecord(BirthRecord birthRecord) {
        birthRecords.add(birthRecord);
        birthRecord.setParentRelationship(this);
        invalidateCaches();
    }


    public void removeBirthRecord(BirthRecord br) {
        birthRecords.remove(br);
        br.setParentRelationship(null); // helps orphanRemoval trigger reliably
        invalidateCaches();
    }


    public <A extends Enum<A> & Allele> Map<AllelePair<A>, Double> getJointDistribution(Category category) {
        checkDirtyJointCache();

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<AlleleCodePair, Double> raw = jointDistributionCache.getOrDefault(category, Map.of());

        Map<AllelePair<A>, Double> result = new HashMap<>();
        for (Map.Entry<AlleleCodePair, Double> entry : raw.entrySet()) {
            AlleleCodePair codePair = entry.getKey();
            Double probability = entry.getValue();

            AllelePair<A> allelePair = new AllelePair<>(
                    domain.parse(codePair.first()),
                    domain.parse(codePair.second())
            );

            result.put(allelePair, probability);
        }

        return result;
    }


    @Transactional
    public void setJointDistribution(Category category, Map<GradePair, Double> jointDistribution) {
        // no op
    }


    // return the phenotype frequencies of the current epoch
    public <A extends Enum<A> & Allele> Map<A, Integer> getCurrentPhenotypeFrequencies(Category category) {
        checkDirtyFrequencyCache();

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        Map<AlleleCodePair, Map<String, Integer>> epochFreq =
                phenotypeFrequencyCache.getOrDefault(category, Map.of());

        AlleleCodePair parents = new AlleleCodePair(
                parent1.getPhenotype(category).code(),
                parent2.getPhenotype(category).code()
        );

        Map<String, Integer> raw = epochFreq.getOrDefault(parents, Map.of());

        Map<A, Integer> result = new EnumMap<>(domain.getAlleleType());
        for (Map.Entry<String, Integer> entry : raw.entrySet()) {
            result.put(domain.parse(entry.getKey()), entry.getValue());
        }

        return result;
    }


    public Map<Category, Map<String, Integer>> getAllCurrentPhenotypeFrequencies() {
        Map<Category, Map<String, Integer>> result = new EnumMap<>(Category.class);
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> cache = copyFrequencyCache();
        for (Category category : Category.values()) {
            AlleleCodePair currentPair = new AlleleCodePair(
                parent1.getPhenotype(category).code(),
                parent2.getPhenotype(category).code()
        );
            Map<AlleleCodePair, Map<String, Integer>> epochFreq = cache.get(category);
            result.put(category, epochFreq.getOrDefault(currentPair, new HashMap<>()));
        }
        return result;
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
            throw new IllegalStateException("Sheep is not a child of this relationship, id: " + this.id);
        }

        birthRecords.remove(birthRecord);
        child.setBirthRecord(null);
        birthRecord.setChild(null);
        birthRecord.setParentRelationship(null);
        invalidateCaches();
    }


    // TODO - update for birthRecords when constraints are figured out
    public void updateChildPhenotypeFrequencies(Sheep child, Map<Category, SheepGenotypeDTO> updatedGenotypes) {
//        BirthRecord birthRecord = child.getBirthRecord();
//        if (birthRecord == null || !this.equals(birthRecord.getParentRelationship())) throw new IllegalArgumentException("Sheep is not a child of this relationship");
//        if (updatedGenotypes == null || updatedGenotypes.isEmpty()) return;
//
//        Map<Category, GradePair> phenotypeDeltas = new EnumMap<>(Category.class);
//        for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
//            Category category = entry.getKey();
//            GradePair genotype = entry.getValue().toGradePair();
//
//            Grade currentPhenotype = child.getPhenotype(category);
//            Grade newPhenotype = genotype.getFirst();
//            phenotypeDeltas.compute(category, (k, v) -> currentPhenotype != newPhenotype ? new GradePair(currentPhenotype, newPhenotype) : null);
//        }
//
//        checkForExcessAlleles(phenotypeDeltas);
//
//        for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
//            Category category = entry.getKey();
//            GradePair genotype = entry.getValue().toGradePair();
//            child.setGenotype(category, genotype);
//        }
//        for (Map.Entry<Category, GradePair> entry : phenotypeDeltas.entrySet()) {
//            Map<Grade, RelationshipPhenotypeFrequency> freqMap = getPhenotypeFrequenciesByCategory(entry.getKey());
//            GradePair delta = entry.getValue();
//            freqMap.get(delta.getFirst()).removeFrequency(1);
//            freqMap.get(delta.getSecond()).addFrequency(1);
//        }
    }


    private void checkForExcessAllelesExperimental(Map<Category, SheepGenotypeDTO> childGenotypes) {
        List<ExcessAlleleViolation> violations = new ArrayList<>();

        for (Category category : Category.values()) {
            checkForExcessAllelesForCategory(category, childGenotypes, violations);
        }

        if (!violations.isEmpty()) {
            throw new ExcessAlleleDiversityException(
                    "Adding these alleles would result in excessive allele diversity",
                    violations
            );
        }
    }


    private <A extends Enum<A> & Allele> void checkForExcessAllelesForCategory(
        Category category,
        Map<Category, SheepGenotypeDTO> childGenotypes,
        List<ExcessAlleleViolation> violations
    ) {
        SheepGenotypeDTO childGenotype = childGenotypes.get(category);
        if (childGenotype == null || childGenotype.phenotype() == null) {
            return;
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        A newAllele = domain.parse(childGenotype.phenotype());
        A parent1Phenotype = parent1.getPhenotype(category);
        A parent2Phenotype = parent2.getPhenotype(category);

        AllelePair<A> currentParentPhenotypes = new AllelePair<>(parent1Phenotype, parent2Phenotype);

        if (newAllele == currentParentPhenotypes.getFirst() || newAllele == currentParentPhenotypes.getSecond()) {
            return;
        }

        Map<AllelePair<A>, Map<A, Integer>> epochMap = getPhenotypeFrequencies(category);
        Set<A> previousHidden = hiddenAlleleDiversity(epochMap, domain);

        if (previousHidden.size() == 2 && !previousHidden.contains(newAllele)) {
            Set<A> validAlleles = EnumSet.of(
                    currentParentPhenotypes.getFirst(),
                    currentParentPhenotypes.getSecond()
            );
            validAlleles.addAll(previousHidden);

            Set<String> validAlleleCodes = validAlleles.stream()
                .map(Allele::code)
                .collect(Collectors.toSet());

            violations.add(
                new ExcessAlleleViolation(
                        category,
                        newAllele.code(),
                        validAlleleCodes
                )
            );
        }
    }


    private <A extends Enum<A> & Allele> Set<A> hiddenAlleleDiversity(
        Map<AllelePair<A>, Map<A, Integer>> epochMap,
        AlleleDomain<A> domain
    ) {
        if (epochMap == null || epochMap.isEmpty()) {
            return EnumSet.noneOf(domain.getAlleleType());
        }

        Set<A> definitiveHiddenAlleles = EnumSet.noneOf(domain.getAlleleType());

        for (Map.Entry<AllelePair<A>, Map<A, Integer>> entry : epochMap.entrySet()) {
            AllelePair<A> parentPhenotypes = entry.getKey();
            Map<A, Integer> phenotypesSeen = entry.getValue();

            for (Map.Entry<A, Integer> freqPair : phenotypesSeen.entrySet()) {
                A phenotype = freqPair.getKey();
                Integer freq = freqPair.getValue();

                if (phenotype != parentPhenotypes.getFirst()
                        && phenotype != parentPhenotypes.getSecond()
                        && freq != null
                        && freq > 0) {
                    definitiveHiddenAlleles.add(phenotype);
                }
            }
        }

        return definitiveHiddenAlleles;
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
