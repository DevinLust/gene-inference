// Categories sheep can have
export type Category = "SWIM" | "FLY" | "RUN" | "POWER" | "STAMINA";

// Distribution types
type DistributionType = "PRIOR" | "INFERRED";

// Grades of certain categories
export type Grade = "S" | "A" | "B" | "C" | "D" | "E";

export type GradePairKey = `(${Grade}, ${Grade})`;

// Map of probabilities of grades across categories and their distribution types
type ProbabilityMap = {
    [C in Category]: {
        [D in DistributionType]: {
            [G in Grade]: number;
        };
    };
};

type ProbabilityMapCreateDTO = {
    [C in Category]?: {
        [G in Grade]: number;
    };
} | null;

// Maps the phenotype and hidden alleles of sheep in each category
type GenotypeMap = {
    [C in Category]: {
        "phenotype": Grade;
        "hiddenAllele": Grade | null;
    }
};

export type Sheep = {
    id: number;
    name: string | null;
    distributions: ProbabilityMap;
    genotypes: GenotypeMap;
    parentRelationshipId: number | null;
};

export type SheepSummary = {
    id: number;
    name: string | null;
};

export type SheepCreateDTO = {
    name: string | null;
    distributions: ProbabilityMapCreateDTO;
    genotypes: GenotypeMap;
    parentRelationshipId: number | null;
};

export type PhenotypeDistributions = {
    [C in Category]: {
        [G in Grade]: number;
    };
};

export type Prediction = {
    phenotypeDistributions: PhenotypeDistributions
};

export type BestPrediction = {
    parent1: SheepSummary;
    parent2: SheepSummary;
    parent1BestCategoryGradeMap: Partial<Record<Category, Grade>>;
    parent2BestCategoryGradeMap: Partial<Record<Category, Grade>>;
    bestCategoriesSet: Category[];
    phenotypeDistributions: PhenotypeDistributions
};

export type PhenotypeFrequencies = Record<
    Category,
    Record<GradePairKey,
        Partial<Record<Grade, number>>>>

export type Relationship = {
    id: number;
    parent1: SheepSummary;
    parent2: SheepSummary;
    phenotypeFrequencies: PhenotypeFrequencies;
};
