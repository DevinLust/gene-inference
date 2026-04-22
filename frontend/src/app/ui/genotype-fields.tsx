import {
    ALL_CATEGORIES,
    APPEARANCE_CATEGORIES,
    TRAIT_CATEGORIES,
    CATEGORY_ALLELE_OPTIONS,
    Category,
    ExcessAlleleViolationDTO,
} from "@/app/lib/definitions";
import CategoryTag from "@/app/ui/category-tag";

export type ControlledGenotypeMap = Record<
    Category,
    {
        phenotype: string;
        hiddenAllele: string;
    }
>;

type GenotypeFieldsProps = {
    genotypes: ControlledGenotypeMap;
    setGenotypes: React.Dispatch<React.SetStateAction<ControlledGenotypeMap>>;
    validationErrors?: Partial<Record<Category, string[]>>;
    constraintViolations?: Partial<Record<Category, ExcessAlleleViolationDTO>>;
};

export default function GenotypeFields({
                                           genotypes,
                                           setGenotypes,
                                           validationErrors,
                                           constraintViolations,
                                       }: GenotypeFieldsProps) {
    return (
        <fieldset className="border border-gray-500 bg-gray-800 p-3 rounded-lg">
            <legend className="font-semibold">Genotypes</legend>

            <CategorySection
                title="Appearance Genes"
                subtleClassName="border-l-2 border-slate-400/40 pl-2"
                categories={APPEARANCE_CATEGORIES}
                genotypes={genotypes}
                setGenotypes={setGenotypes}
                validationErrors={validationErrors}
                constraintViolations={constraintViolations}
            />

            <CategorySection
                title="Stat Genes"
                subtleClassName="border-l-2 border-blue-300/30 pl-2"
                categories={TRAIT_CATEGORIES}
                genotypes={genotypes}
                setGenotypes={setGenotypes}
                validationErrors={validationErrors}
                constraintViolations={constraintViolations}
            />
        </fieldset>
    );
}

type CategorySectionProps = {
    title: string;
    subtleClassName?: string;
    categories: Category[];
    genotypes: ControlledGenotypeMap;
    setGenotypes: React.Dispatch<React.SetStateAction<ControlledGenotypeMap>>;
    validationErrors?: Partial<Record<Category, string[]>>;
    constraintViolations?: Partial<Record<Category, ExcessAlleleViolationDTO>>;
};

function CategorySection({
                             title,
                             subtleClassName = "",
                             categories,
                             genotypes,
                             setGenotypes,
                             validationErrors,
                             constraintViolations,
                         }: CategorySectionProps) {
    return (
        <div className={`mt-2 ${subtleClassName}`}>
            <div className="mb-2 text-sm font-medium text-gray-300">{title}</div>

            <div className="flex flex-col gap-2">
                {categories.map((c) => {
                    const options = CATEGORY_ALLELE_OPTIONS[c];
                    const categoryValidationErrors = validationErrors?.[c];
                    const categoryConstraintViolation = constraintViolations?.[c];

                    return (
                        <div key={c} className="bg-blue-900 border border-gray-500 rounded-lg">
                            <div className="w-full bg-blue-500 pl-4 py-1 rounded-t-lg">
                                <CategoryTag category={c} />
                            </div>

                            <div className="grid grid-cols-[auto_minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-x-4 gap-y-2 p-4">
                                <label htmlFor={`genotypes.${c}.phenotype`} className="whitespace-nowrap">
                                    Phenotype:
                                </label>
                                <select
                                    id={`genotypes.${c}.phenotype`}
                                    name={`genotypes.${c}.phenotype`}
                                    value={genotypes[c]?.phenotype ?? ""}
                                    onChange={(e) =>
                                        setGenotypes((prev) => ({
                                            ...prev,
                                            [c]: {
                                                ...prev[c],
                                                phenotype: e.target.value,
                                            },
                                        }))
                                    }
                                    className="w-full min-w-0 py-1 border border-gray-500 rounded bg-gray-800"
                                >
                                    <option value="">Select...</option>
                                    {options.map((opt) => (
                                        <option key={opt.value} value={opt.value}>
                                            {opt.label}
                                        </option>
                                    ))}
                                </select>

                                <label htmlFor={`genotypes.${c}.hiddenAllele`} className="whitespace-nowrap">
                                    Hidden Allele:
                                </label>
                                <select
                                    id={`genotypes.${c}.hiddenAllele`}
                                    name={`genotypes.${c}.hiddenAllele`}
                                    value={genotypes[c]?.hiddenAllele ?? ""}
                                    onChange={(e) =>
                                        setGenotypes((prev) => ({
                                            ...prev,
                                            [c]: {
                                                ...prev[c],
                                                hiddenAllele: e.target.value,
                                            },
                                        }))
                                    }
                                    className="w-full min-w-0 py-1 border border-gray-500 rounded bg-gray-800"
                                >
                                    <option value="">None</option>
                                    {options.map((opt) => (
                                        <option key={opt.value} value={opt.value}>
                                            {opt.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div id={`genotypes-${c}-error`} aria-live="polite" aria-atomic="true">
                                {categoryValidationErrors?.map((err) => (
                                    <p key={err} className="ml-2 text-sm text-yellow-500">
                                        {err}
                                    </p>
                                ))}

                                {categoryConstraintViolation && (
                                    <div className="ml-2">
                                        <p className="text-sm text-yellow-500">
                                            Attempted to record: {categoryConstraintViolation.attemptedAllele}
                                        </p>
                                        <p className="text-sm text-yellow-500">
                                            Possible alleles: {categoryConstraintViolation.validAlleles.join(", ")}
                                        </p>
                                    </div>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

export function createEmptyControlledGenotypes(): ControlledGenotypeMap {
    return ALL_CATEGORIES.reduce((acc, c) => {
        acc[c] = { phenotype: "", hiddenAllele: "" };
        return acc;
    }, {} as ControlledGenotypeMap);
}
