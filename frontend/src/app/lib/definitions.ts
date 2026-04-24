// Categories sheep can have
export const ALL_CATEGORIES = [
    "SWIM",
    "FLY",
    "RUN",
    "POWER",
    "STAMINA",
    "TONE",
    "COLOR",
    "SHINY",
] as const;

export type Category = typeof ALL_CATEGORIES[number];

export const CATEGORY_LABELS: Record<Category, string> = {
    SWIM: "Swim",
    FLY: "Fly",
    RUN: "Run",
    POWER: "Power",
    STAMINA: "Stamina",
    TONE: "Tone",
    COLOR: "Color",
    SHINY: "Shiny",
};

export const TRAIT_CATEGORIES: Category[] = [
    "SWIM",
    "FLY",
    "RUN",
    "POWER",
    "STAMINA",
];

export const APPEARANCE_CATEGORIES: Category[] = [
    "COLOR",
    "TONE",
    "SHINY",
];

export const CATEGORY_COLORS: Record<Category, string> = {
    SWIM: "#b8aa11",
    FLY: "#ac0db8",
    RUN: "#047a21",
    POWER: "#a30f1b",
    STAMINA: "#ab690e",
    TONE: "#2563eb",
    COLOR: "#db2777",
    SHINY: "#9ca3af",
};

// Distribution types
export type DistributionType = "PRIOR" | "INFERRED";

// General Alleles
export type AlleleCode = string;
export type AlleleCodePairKey = string; // e.g. "SHN|NRM"

export const ALLELE_LABELS: Partial<Record<Category, Record<string, string>>> = {
    SHINY: {
        SHN: "Shiny",
        NRM: "Non-Shiny",
    },
    COLOR: {
        NRM: "Normal",
        RED: "Red",
        BLU: "Blue",
        YEL: "Yellow",
        PNK: "Pink",
        PUR: "Purple",
        SKY: "Sky Blue",
        ORA: "Orange",
        GRN: "Green",
        BRN: "Brown",
        GRY: "Grey",
        LIM: "Lime Green",
        BLK: "Black",
        WHT: "White",
    },
    TONE: {
        M: "Monotone",
        T: "Two Tone",
    },
};

export const CATEGORY_ALLELE_OPTIONS: Record<Category, { value: string; label: string }[]> = {
    SWIM: [
        { value: "S", label: "S" },
        { value: "A", label: "A" },
        { value: "B", label: "B" },
        { value: "C", label: "C" },
        { value: "D", label: "D" },
        { value: "E", label: "E" },
    ],
    FLY: [
        { value: "S", label: "S" },
        { value: "A", label: "A" },
        { value: "B", label: "B" },
        { value: "C", label: "C" },
        { value: "D", label: "D" },
        { value: "E", label: "E" },
    ],
    RUN: [
        { value: "S", label: "S" },
        { value: "A", label: "A" },
        { value: "B", label: "B" },
        { value: "C", label: "C" },
        { value: "D", label: "D" },
        { value: "E", label: "E" },
    ],
    POWER: [
        { value: "S", label: "S" },
        { value: "A", label: "A" },
        { value: "B", label: "B" },
        { value: "C", label: "C" },
        { value: "D", label: "D" },
        { value: "E", label: "E" },
    ],
    STAMINA: [
        { value: "S", label: "S" },
        { value: "A", label: "A" },
        { value: "B", label: "B" },
        { value: "C", label: "C" },
        { value: "D", label: "D" },
        { value: "E", label: "E" },
    ],
    TONE: [
        { value: "M", label: "Monotone" },
        { value: "T", label: "Two Tone" },
    ],
    COLOR: [
        { value: "NRM", label: "Normal" },
        { value: "WHT", label: "White" },
        { value: "RED", label: "Red" },
        { value: "BLU", label: "Blue" },
        { value: "YEL", label: "Yellow" },
        { value: "PNK", label: "Pink" },
        { value: "PUR", label: "Purple" },
        { value: "SKY", label: "Sky Blue" },
        { value: "ORA", label: "Orange" },
        { value: "GRN", label: "Green" },
        { value: "BRN", label: "Brown" },
        { value: "GRY", label: "Grey" },
        { value: "LIM", label: "Lime Green" },
        { value: "BLK", label: "Black" },
    ],
    SHINY: [
        { value: "NRM", label: "Non-Shiny" },
        { value: "SHN", label: "Shiny" },
    ],
};

export function displayAllele(category: Category, code: string | null | undefined): string {
    if (!code) return "unknown";
    return ALLELE_LABELS[category]?.[code] ?? code;
}

export function parseAlleleCodePair(key: AlleleCodePairKey): [AlleleCode, AlleleCode] {
    const parts = key.split("|");
    if (parts.length !== 2) {
        throw new Error(`Invalid AlleleCodePairKey: ${key}`);
    }
    return [parts[0], parts[1]];
}

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
export type ProbabilityMap = {
    [C in Category]: {
        [D in DistributionType]: Record<AlleleCode, number>;
    };
};

export type ProbabilityMapCreateDTO = {
    [C in Category]?: Record<AlleleCode, number>;
} | null;

// Maps the phenotype and hidden alleles of sheep in each category
export type GenotypeMap = {
    [C in Category]: {
        phenotype: AlleleCode;
        hiddenAllele: AlleleCode | null;
    };
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
    lockedCategories: Category[];
};

export type SheepSummary = {
    id: number;
    name: string | null;
};

export type SheepFilter = {
    grades?: Grade[];
    name?: string;
    ids?: number[];
}

export type SheepCreateDTO = {
    name: string | null;
    genotypes: GenotypeMap | null;
};

export type SheepChildDTO = {
    name: string | null;
    genotypes: GenotypeMap | null;
    parent1Id: number;
    parent2Id: number;
}

export type SheepUpdateDTO = {
    name: string | null;
    genotypes: GenotypeMap | null;
}

export type PhenotypeDistributions = {
    [C in Category]: Record<AlleleCode, number>;
};

export type Distributions = {
    category: Category;
    type: DistributionType;
    distributions: Record<string, Record<AlleleCode, number>>;
};

export type DistributionFilter = {
    category: Category;
    type: DistributionType;
    ids?: number[];
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
    Record<AlleleCodePairKey, Partial<Record<AlleleCode, number>>>
>;

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
    parent1Code: AlleleCode;
    parent2Code: AlleleCode;
    childCode: AlleleCode;
};

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

export type ValidationFailed = {
    ok?: false
    message?: string
    error: "VALIDATION_FAILED";
    errors?: {
        name?: string[];
        parent1Id?: string[];
        parent2Id?: string[];
        genotypes?: Partial<Record<Category, string[]>>
    };
    suggestions?: string[];
};

export type ExcessAlleleViolationDTO = {
    reason: string;
    message: string;
    attemptedAllele?: AlleleCode;
    validAlleles?: AlleleCode[];
};

export type GeneticConstraintViolation = {
    ok?: false;
    message?: string;
    error: "GENETIC_CONSTRAINT_VIOLATION";
    errors: {
        genotypes: Partial<Record<Category, ExcessAlleleViolationDTO>>;
    };
    suggestions?: string[];
};


// loopy belief visualization types
export type LogEntry = {
    kind: RunEventType;
    text: string;
};

export type RunEventType = "RUN_STARTED" | "STEP_EVENT" | "COMPLETED";
export type RunStage = "MESSAGE_PASSING" | "BELIEF_UPDATE" | "COMPLETED";
export type RunSource = "USER" | "DEMO";

export type VisualNode = {
    id: string;
    type: "sheep" | "relationship";
    label: string;
    x: number;
    y: number;
    center: boolean;
};

export type VisualEdge = {
    id: string;
    sourceId: string;
    targetId: string;
    type: "full" | "stub";
    visibleTarget: boolean;
    relationshipRole?: "PARENT" | "CHILD" | null;
    stubAngleRadians?: number | null;
    stubIndex?: number | null;
    stubCount?: number | null;
};

export type VisualGraphSnapshot = {
    centerSheepId: string;
    nodes: VisualNode[];
    edges: VisualEdge[];
};

export type MessageWaveDelta = {
    waveType: "SHEEP_TO_RELATIONSHIP" | "RELATIONSHIP_TO_SHEEP";
    category: string;
    activeFullEdgeIds: string[];
    activeStubEdgeIds: string[];
};

export type RunStartedPayload = {
    totalSteps: number;
    currentStep: number;
    stage: "MESSAGE_PASSING" | "BELIEF_UPDATE" | "COMPLETED";
    graph: VisualGraphSnapshot;
};

export type StepEventPayload = {
    stepIndex: number;
    totalSteps: number;
    stage: RunStage;
    message: string;
    delta?: MessageWaveDelta | null;
};

export type CompletedPayload = {
    message: string;
};

export type RunEvent = {
    type: RunEventType;
    runId: string;
    payload: RunStartedPayload | StepEventPayload | CompletedPayload;
};
