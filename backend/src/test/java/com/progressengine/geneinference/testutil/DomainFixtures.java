package com.progressengine.geneinference.testutil;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

import java.util.*;

public class DomainFixtures {
    public static final UUID TEST_USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static Sheep createTestSheep(Map<Category, String> phenotypes) {
        Sheep sheep = new Sheep();

        for (Category category : Category.values()) {
            String phenotypeCode = phenotypes.getOrDefault(category, defaultPhenotypeCode(category));
            sheep.setPhenotypeCode(category, phenotypeCode);
        }

        sheep.createDefaultDistributions();
        return sheep;
    }

    public static Sheep createTestSheep(Map<Category, String> phenotypes, int sheepId) {
        Sheep sheep = createTestSheep(phenotypes);
        sheep.setId(sheepId);
        return sheep;
    }

    public static Sheep createTestSheep(UUID userId, Map<Category, String> phenotypes) {
        Sheep sheep = createTestSheep(phenotypes);
        sheep.setUserId(userId);
        return sheep;
    }

    public static Sheep createTestSheepWithFullGenotype(Map<Category, SheepGenotypeDTO> phenotypes) {
        Sheep sheep = new Sheep();

        for (Category category : Category.values()) {
            SheepGenotypeDTO genotypeDTO = phenotypes.getOrDefault(category, defaultGenotype(category));
            AllelePair<?> genotype = genotypeDTO.toAllelePair(category);
            sheep.setGenotype(category, genotype);
        }

        sheep.createDefaultDistributions();
        return sheep;
    }

    public static Relationship createEmptyRelationship(Sheep parent1, Sheep parent2) {
        Relationship relationship = new Relationship();
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        return relationship;
    }

    public static Relationship createTestRelationship(Sheep parent1, Sheep parent2, Map<Category, Map<String, Integer>> offspringPhenotypeFrequency) {
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> modifiedFreq = new EnumMap<>(Category.class);
        for (Category category : offspringPhenotypeFrequency.keySet()) {
            AlleleCodePair pair = new AlleleCodePair(parent1.getPhenotype(category), parent2.getPhenotype(category));
            modifiedFreq.computeIfAbsent(category, k -> new HashMap<>())
                    .put(pair, offspringPhenotypeFrequency.get(category));
        }
        return createPopulatedRelationship(parent1, parent2, modifiedFreq);
    }

    public static Relationship createTestRelationship(Sheep parent1, Sheep parent2, Map<Category, Map<String, Integer>> offspringPhenotypeFrequency, int relationshipId) {
        Relationship relationship = createTestRelationship(parent1, parent2, offspringPhenotypeFrequency);
        relationship.setId(relationshipId);
        return relationship;
    }

    public static Relationship createPopulatedRelationship(Sheep parent1, Sheep parent2, Map<Category, Map<AlleleCodePair, Map<String, Integer>>> offspringPhenotypeFrequency) {
        if (offspringPhenotypeFrequency == null) {
            return createEmptyRelationship(parent1, parent2);
        }

        // validate totals per category are equal
        Integer expectedTotal = null;
        for (Category cat : offspringPhenotypeFrequency.keySet()) {
            int total = sumCategory(offspringPhenotypeFrequency.get(cat));
            if (expectedTotal == null) expectedTotal = total;
            else if (total != expectedTotal) {
                throw new IllegalArgumentException(
                        "All categories must have the same total count. " +
                                "Category " + cat + " has " + total + " but expected " + expectedTotal
                );
            }
        }

        int birthsToCreate = expectedTotal != null ? expectedTotal : 0;
        if (birthsToCreate == 0) {
            return createEmptyRelationship(parent1, parent2);
        }

        Relationship relationship = new Relationship();
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> work = deepCopyMap(offspringPhenotypeFrequency);

        for (int i = 0; i < birthsToCreate; i++) {
            BirthRecord br = new BirthRecord();
            br.setParentRelationship(relationship);
            br.setChild(null);

            for (Category cat : work.keySet()) {
                Choice choice = pickAndDecrement(work, cat);

                BirthRecordPhenotype brp = new BirthRecordPhenotype(br, cat);
                brp.setAllPhenotypeCodes(choice.parents, choice.child);
                br.getPhenotypesAtBirth().add(brp);
            }
            relationship.addBirthRecord(br);
        }

        return relationship;
    }

    private static String defaultPhenotypeCode(Category category) {
        return switch (category) {
            case SWIM, FLY, RUN, POWER, STAMINA -> "C";
            case TONE -> "T";
            case COLOR, SHINY -> "NRM";
            default -> CategoryDomains.domainFor(category).getAlleles().getFirst().code();
        };
    }

    private static SheepGenotypeDTO defaultGenotype(Category category) {
        String fallBack = defaultPhenotypeCode(category);
        return new SheepGenotypeDTO(fallBack, fallBack);
    }

    private static int sumCategory(Map<AlleleCodePair, Map<String, Integer>> categoryFrequency) {
        if (categoryFrequency == null || categoryFrequency.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Map<String, Integer> epochMap : categoryFrequency.values()) {
            if (epochMap == null || epochMap.isEmpty()) { continue; }
            total += epochMap.values().stream().reduce(0, Integer::sum);
        }
        return total;
    }

    private static Map<Category, Map<AlleleCodePair, Map<String, Integer>>> deepCopyMap(Map<Category, Map<AlleleCodePair, Map<String, Integer>>> map) {
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> result = new EnumMap<>(Category.class);
        for (Category category : map.keySet()) {
            result.put(category, new HashMap<>());
            Map<AlleleCodePair, Map<String, Integer>> categoryFreq = map.getOrDefault(category, new HashMap<>());
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

    private record Choice(AlleleCodePair parents, String child) {}

    private static Choice pickAndDecrement(Map<Category, Map<AlleleCodePair, Map<String, Integer>>> map, Category category) {
        Map<AlleleCodePair, Map<String, Integer>> categoryFreq = map.get(category);
        if (categoryFreq == null || categoryFreq.isEmpty()) {
            throw new IllegalArgumentException("Category " + category + " has no remaining entries to draw from");
        }

        Iterator<Map.Entry<AlleleCodePair, Map<String, Integer>>> pairIt = categoryFreq.entrySet().iterator();
        while (pairIt.hasNext()) {
            Map.Entry<AlleleCodePair, Map<String, Integer>> pair = pairIt.next();
            AlleleCodePair parents = pair.getKey();
            Map<String, Integer> epochFrequency = pair.getValue();

            Iterator<Map.Entry<String, Integer>> gradeIt = epochFrequency.entrySet().iterator();
            while (gradeIt.hasNext()) {
                Map.Entry<String, Integer> gradeEntry = gradeIt.next();
                String childGrade = gradeEntry.getKey();
                int count = gradeEntry.getValue();

                if (count <= 0) {
                    gradeIt.remove();
                    continue;
                }

                int newCount = count - 1;
                if (newCount == 0) { gradeIt.remove(); }
                else gradeEntry.setValue(newCount);

                if (epochFrequency.isEmpty()) pairIt.remove();

                return new Choice(parents, childGrade);
            }
        }
        throw new IllegalArgumentException("Category " + category + " ran out of entries before expected total");
    }
}
