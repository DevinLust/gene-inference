// Categories sheep can have
export type Category = "SWIM" | "FLY" | "RUN" | "POWER" | "STAMINA";

// Distribution types
export type DistributionType = "PRIOR" | "INFERRED";

// Grades of certain categories
export type Grade = "S" | "A" | "B" | "C" | "D" | "E";

export type GradePairKey = `(${Grade}, ${Grade})`;

export function parseGradePair(key: GradePairKey): [Grade, Grade] {
    const [g1, g2] = key
        .slice(1, -1)   // remove "(" and ")"
        .split(", ")
        .map(s => s as Grade);

    return [g1, g2];
}

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

export type PageRequest = {
    page?: number;              // 0-based to match Spring
    size?: number;
    sort?: string | string[];   // e.g. "id,desc" or ["id,desc","name,asc"]
};

export type PageResponse<T> = {
    items: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    hasNext: boolean
}

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

export type SheepFilter = {
    grades?: string;
    name?: string;
    ids?: string;
}

export type SheepCreateDTO = {
    name: string | null;
    distributions: ProbabilityMapCreateDTO;
    genotypes: GenotypeMap;
    parentRelationshipId: number | null;
};

export type SheepChildDTO = {
    name: string | null;
    distributions: ProbabilityMapCreateDTO;
    genotypes: GenotypeMap;
    parent1Id: number;
    parent2Id: number;
}

export type SheepUpdateDTO = {
    name: string | null;
    distributions: ProbabilityMapCreateDTO;
    genotypes: GenotypeMap;
}

export type PhenotypeDistributions = {
    [C in Category]: {
        [G in Grade]: number;
    };
};

export type Distributions = {
    category: Category;
    type: DistributionType;
    distributions: Record<string, Record<Grade, number>>;
}

export type DistributionFilter = {
    category: Category;
    type: DistributionType;
    ids?: string;
}

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

export type RelationshipRow = {
    id: number;
    parent1Id: number;
    parent2Id: number;
    parent1Name: string | null;
    parent2Name: string | null;
}

export type PhenotypesAtBirth = {
    parent1: Grade;
    parent2: Grade;
    child: Grade;
}

export type RelationshipSummary = {
    id: number;
    parent1: SheepSummary;
    parent2: SheepSummary;
}

export type BirthRecord = {
    id: number;
    parentRelationshipSummary: RelationshipSummary;
    child: SheepSummary | null;
    phenotypesAtBirth: Record<Category, PhenotypesAtBirth>;
}

export type BirthRecordRow = {
    id: number;
    relationshipId: number;
    childId: number | null;
    childName: string | null;
    parent1Id: number;
    parent1Name: string | null;
    parent2Id: number;
    parent2Name: string | null;
}

export type BirthRecordFilter = {
    relationshipId?: number;
    category?: Category;
    p1?: Grade;
    p2?: Grade;
} & PageRequest;
