package com.progressengine.geneinference.testutil;

import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DomainFixtures {
    public static Sheep createTestSheep(Map<Category, Grade> phenotypes) {
        Sheep sheep = new Sheep();
        for (Map.Entry<Category, Grade> entry : phenotypes.entrySet()) {
            sheep.setPhenotype(entry.getKey(), entry.getValue());
        }
        sheep.createDefaultDistributions();
        return sheep;
    }
    public static Sheep createTestSheep(Map<Category, Grade> phenotypes, int sheepId) {
        Sheep sheep = createTestSheep(phenotypes);
        sheep.setId(sheepId);
        return sheep;
    }

    public static Relationship createTestRelationship(Sheep parent1, Sheep parent2, Map<Category, Map<Grade, Integer>> offspringPhenotypeFrequency) {
        Relationship relationship = new Relationship();
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        if (offspringPhenotypeFrequency != null) {
            for (Map.Entry<Category, Map<Grade, Integer>> entry : offspringPhenotypeFrequency.entrySet()) {
                relationship.setPhenotypeFrequencies(entry.getKey(), entry.getValue());
            }
        }
        return relationship;
    }

    public static Relationship createTestRelationship(Sheep parent1, Sheep parent2, Map<Category, Map<Grade, Integer>> offspringPhenotypeFrequency, int relationshipId) {
        Relationship relationship = createTestRelationship(parent1, parent2, offspringPhenotypeFrequency);
        relationship.setId(relationshipId);
        return relationship;
    }

    public static Relationship createPopulatedRelationship(Sheep parent1, Sheep parent2, Map<Category, Map<GradePair, Map<Grade, Integer>>> offspringPhenotypeFrequency) {
        // validate totals per category are equal
        Integer expectedTotal = null;
        for (Category cat : Category.values()) {
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
            Relationship empty = new Relationship();
            empty.setParent1(parent1);
            empty.setParent2(parent2);
            return empty;
        }

        Relationship relationship = new Relationship();
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        Map<Category, Map<GradePair, Map<Grade, Integer>>> work = deepCopyMap(offspringPhenotypeFrequency);

        for (int i = 0; i < birthsToCreate; i++) {
            BirthRecord br = new BirthRecord();
            br.setParentRelationship(relationship);
            br.setChild(null);

            for (Category cat : Category.values()) {
                Choice choice = pickAndDecrement(work, cat);

                BirthRecordPhenotype brp = new BirthRecordPhenotype(br, cat);
                brp.setAllPhenotypes(choice.parents, choice.child);
                br.getPhenotypesAtBirth().add(brp);
            }
            relationship.addBirthRecord(br);
        }

        return relationship;
    }

    private static int sumCategory(Map<GradePair, Map<Grade, Integer>> categoryFrequency) {
        if (categoryFrequency == null || categoryFrequency.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Map<Grade, Integer> epochMap : categoryFrequency.values()) {
            total += epochMap.values().stream().reduce(0, Integer::sum);
        }
        return total;
    }

    private static Map<Category, Map<GradePair, Map<Grade, Integer>>> deepCopyMap(Map<Category, Map<GradePair, Map<Grade, Integer>>> map) {
        Map<Category, Map<GradePair, Map<Grade, Integer>>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, new HashMap<>());
            Map<GradePair, Map<Grade, Integer>> categoryFreq = map.getOrDefault(category, new HashMap<>());
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

    private record Choice(GradePair parents, Grade child) {}

    private static Choice pickAndDecrement(Map<Category, Map<GradePair, Map<Grade, Integer>>> map, Category category) {
        Map<GradePair, Map<Grade, Integer>> categoryFreq = map.get(category);
        if (categoryFreq == null || categoryFreq.isEmpty()) {
            throw new IllegalArgumentException("Category " + category + " has no remaining entries to draw from");
        }

        Iterator<Map.Entry<GradePair, Map<Grade, Integer>>> pairIt = categoryFreq.entrySet().iterator();
        while (pairIt.hasNext()) {
            Map.Entry<GradePair, Map<Grade, Integer>> pair = pairIt.next();
            GradePair parents = pair.getKey();
            Map<Grade, Integer> epochFrequency = pair.getValue();

            Iterator<Map.Entry<Grade, Integer>> gradeIt = epochFrequency.entrySet().iterator();
            while (gradeIt.hasNext()) {
                Map.Entry<Grade, Integer> gradeEntry = gradeIt.next();
                Grade childGrade = gradeEntry.getKey();
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
