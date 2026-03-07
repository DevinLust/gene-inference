package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradeExpressionRules;
import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseInferenceEngine implements InferenceEngine {

    /**
     * Predicts the probabilities of the phenotype for each category of the two parents' children.
     * The two parents don't need to be in a Relationship. The two parents can be the same
     * Sheep.
     *
     * @param parent1 - the first parent Sheep to base the prediction on
     * @param parent2 - the second parent Sheep to base the prediction on
     * @return a Map of distributions that predict the probability of phenotypes for the children
     * of these two parents by category
     */
    public Map<Category, Map<Grade, Double>> predictChildrenDistributions(Sheep parent1, Sheep parent2) {
        Map<Category, Map<Grade, Double>> predictedDistributions = new EnumMap<>(Category.class);

        for (Category category : Category.values()) {
            Map<Grade, Double> parent1AlleleDistribution = inheritedAlleleDistribution(parent1, category);
            Map<Grade, Double> parent2AlleleDistribution = inheritedAlleleDistribution(parent2, category);

            Map<Grade, Double> childPhenotypeDistribution = new EnumMap<>(Grade.class);

            for (Grade expressed : Grade.values()) {
                childPhenotypeDistribution.put(expressed, 0.0);
            }

            for (Map.Entry<Grade, Double> p1Entry : parent1AlleleDistribution.entrySet()) {
                Grade allele1 = p1Entry.getKey();
                double pAllele1 = p1Entry.getValue();

                for (Map.Entry<Grade, Double> p2Entry : parent2AlleleDistribution.entrySet()) {
                    Grade allele2 = p2Entry.getKey();
                    double pAllele2 = p2Entry.getValue();

                    double genotypeProbability = pAllele1 * pAllele2;
                    double[] expressionProbability = GradeExpressionRules.probabilityExpressed(allele1, allele2);

                    childPhenotypeDistribution.merge(
                            allele1,
                            genotypeProbability * expressionProbability[0],
                            Double::sum
                    );
                    childPhenotypeDistribution.merge(
                            allele2,
                            genotypeProbability * expressionProbability[1],
                            Double::sum
                    );
                }
            }

            predictedDistributions.put(category, childPhenotypeDistribution);
        }

        return predictedDistributions;
    }

    private Map<Grade, Double> inheritedAlleleDistribution(Sheep parent, Category category) {
        Map<Grade, Double> result = new EnumMap<>(Grade.class);

        Map<Grade, Double> hiddenDistribution =
                parent.getDistribution(category, DistributionType.INFERRED);

        // 50% chance to pass the visible allele
        result.merge(parent.getPhenotype(category), 0.5, Double::sum);

        // 50% chance to pass the hidden allele, distributed by the inferred hidden distribution
        for (Map.Entry<Grade, Double> entry : hiddenDistribution.entrySet()) {
            result.merge(entry.getKey(), 0.5 * entry.getValue(), Double::sum);
        }

        return result;
    }

    // check the hidden distributions of a sheep and if an allele is certain, set the hidden allele to it
    protected void checkCertainty(Sheep sheep, Category category) {
        if (sheep.getHiddenAllele(category) != null) { return; }

        for (Map.Entry<Grade, Double> entry : sheep.getDistribution(category, DistributionType.INFERRED).entrySet()) {
            if (entry.getValue() == 1.0) { // could make this a certainty threshold
                sheep.setHiddenAllele(category, entry.getKey());
            }
        }
    }

    // Maps a pair of hidden alleles to a conditional distribution based on the observed phenotype
    protected Map<GradePair, Map<Grade, Double>> findConditionalDistributions(Relationship relationship, Grade childPhenotype, Category category) {
        Grade phenotype1 = relationship.getParent1().getPhenotype(category);
        Grade phenotype2 = relationship.getParent2().getPhenotype(category);
        Map<GradePair, Double> jointDistribution = relationship.getJointDistributions().get(category);

        // find the probability distribution of the hidden allele given both genotypes
        Map<GradePair, Map<Grade, Double>> conditionalDistributions = new HashMap<>();
        for (GradePair gradePair : jointDistribution.keySet()) {
            // find the probability the phenotype came from each parent
            double[] parentProbabilities = InferenceMath.probabilityAlleleFromParents(gradePair, phenotype1, phenotype2, childPhenotype);

            // get the distribution of the hidden child allele from the current genotypes
            Map<Grade, Double> genotypeDistribution = new EnumMap<>(Grade.class);
            genotypeDistribution.merge(phenotype1, 0.5 * parentProbabilities[1], Double::sum);
            genotypeDistribution.merge(gradePair.getFirst(), 0.5 * parentProbabilities[1], Double::sum);
            genotypeDistribution.merge(phenotype2, 0.5 * parentProbabilities[0], Double::sum);
            genotypeDistribution.merge(gradePair.getSecond(), 0.5 * parentProbabilities[0], Double::sum);

            conditionalDistributions.put(gradePair, genotypeDistribution);
        }

        return conditionalDistributions;
    }

    @Deprecated
    // Returns a relative multinomial score based on the given hidden alleles, phenotypes, and phenotype frequency seen in the relationship
    protected double multinomialScore(GradePair hiddenPair, Grade phenotype1, Grade phenotype2, Map<Grade, Integer> phenotypeFrequency) {
        double score = 1000000.0; // A multiplicative constant to help keep scores from getting too small quickly

        // each occurrence of a grade adds 1/4 to the probability of that grade
        Map<Grade, Double> probabilityToDraw = new EnumMap<>(Grade.class);
        probabilityToDraw.merge(phenotype1, 0.25, Double::sum);
        probabilityToDraw.merge(phenotype2, 0.25, Double::sum);
        probabilityToDraw.merge(hiddenPair.getFirst(), 0.25, Double::sum);
        probabilityToDraw.merge(hiddenPair.getSecond(), 0.25, Double::sum);

        for (Grade grade : Grade.values()) {
            Double probability = probabilityToDraw.getOrDefault(grade, 0.0);
            Integer frequency = phenotypeFrequency.getOrDefault(grade, 0);
            if (probability == 0.0 && frequency > 0) {
                score = 0.0;
            } else if (frequency > 0) {
                score *= Math.exp(frequency * Math.log(probability));
            }
        }

        return score;
    }

    protected void fillMissingValuesWithZero(Map<Grade, Double> scores) {
        for (Grade grade : Grade.values()) {
            scores.putIfAbsent(grade, 0.0);
        }
    }
}
