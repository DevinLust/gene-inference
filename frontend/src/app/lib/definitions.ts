// Categories sheep can have
export type Category = "SWIM" | "FLY" | "RUN" | "POWER" | "STAMINA";

// Distribution types
type DistributionType = "PRIOR" | "INFERRED";

// Grades of certain categories
export type Grade = "S" | "A" | "B" | "C" | "D" | "E";

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

export type SheepCreateDTO = {
    name: string | null;
    distributions: ProbabilityMapCreateDTO;
    genotypes: GenotypeMap;
    parentRelationshipId: number | null;
}

export type Prediction = {
    [C in Category]: {
        [G in Grade]: number;
    };
};
