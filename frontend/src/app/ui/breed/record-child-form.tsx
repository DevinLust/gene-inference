'use client';

import { useActionState, useState, useEffect, startTransition } from 'react';
import { recordChild, ChildState } from '@/app/lib/actions';
import { SheepSummary, Category, Grade } from '@/app/lib/definitions';
import CategoryTag from '@/app/ui/category-tag';
import SheepComboBox from "./sheep-combo-box";
import { useBreedSheep } from "@/app/(main)/breed/breed-sheep-provider";

const categories: Category[] = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];
const grades: Grade[] = ["S", "A", "B", "C", "D", "E"];

export default function RecordChildForm() {
    const sheep: SheepSummary[] = useBreedSheep();
    const initialState: ChildState = { status: "idle" };
    const [state, formAction, isPending] = useActionState(recordChild, initialState);

    const [saveChild, setSaveChild] = useState(false);

    // controlled selections
    const [parent1Id, setParent1Id] = useState<number | null>(null);
    const [parent2Id, setParent2Id] = useState<number | null>(null);

    // child name (optional)
    const [childName, setChildName] = useState<string>("");

    // controlled genotypes
    const [genotypes, setGenotypes] = useState(() =>
        categories.reduce((acc, c) => {
            acc[c] = { phenotype: "", hiddenAllele: "" };
            return acc;
        }, {} as Record<string, { phenotype: string; hiddenAllele: string }>)
    );

    // hide old server errors once user changes inputs
    const [dirtySinceSubmit, setDirtySinceSubmit] = useState(false);

    useEffect(() => {
        // whenever the server returns a new state,
        // we are no longer "dirty since submit"
        setDirtySinceSubmit(false);
    }, [state]);

    const showServerErrors = !dirtySinceSubmit;

    const errorState = showServerErrors && isErrorState(state) ? state : null;

    const validationErrors = errorState && isValidationFailed(errorState) ? errorState.errors : null;
    const constraintErrors = errorState && isGeneticConstraint(errorState) ? errorState.errors : null;

    const suggestions = errorState?.suggestions;
    const serverMessage = errorState?.message;

    return (
        <form
            onSubmit={(e) => {
                e.preventDefault()
                if (isPending) return

                const formData = new FormData(e.currentTarget)
                startTransition(() => {
                    formAction(formData)
                })
            }}
            className="flex flex-col gap-6 p-4 bg-gray-600 rounded-lg w-fit"
        >
            <h2 className="text-lg font-semibold">Record External Event</h2>

            <div className="flex justify-start gap-2">
            <div className="max-w-md">
                {/* Parents */}
                <div className={"grid grid-cols-1 gap-4"}>
                    {/* Parent 1 */}
                    <div>
                        <SheepComboBox
                            sheep={sheep}
                            inputLabel="Parent 1"
                            fieldName="parent1Id"
                            selectedId={parent1Id}
                            onSelect={(id) => {
                                setParent1Id(id);
                                setDirtySinceSubmit(true);
                            }}
                        />

                        <div id="parent1-error" aria-live="polite" aria-atomic="true">
                            {validationErrors?.parent1Id?.map((error) => (
                                <p key={error} className="mt-2 text-sm text-yellow-500">{error}</p>
                            ))}
                        </div>
                    </div>

                    {/* Parent 2 */}
                    <div>
                        <SheepComboBox
                            sheep={sheep}
                            inputLabel="Parent 2"
                            fieldName="parent2Id"
                            selectedId={parent2Id}
                            onSelect={(id) => {
                                setParent2Id(id);
                                setDirtySinceSubmit(true);
                            }}
                        />

                        <div id="parent2-error" aria-live="polite" aria-atomic="true">
                            {validationErrors?.parent2Id?.map((error) => (
                                <p key={error} className="mt-2 text-sm text-yellow-500">{error}</p>
                            ))}
                        </div>
                    </div>
                </div>


                {/* Save child checkbox */}
                <div className="flex flex-col gap-1">
                    <label className="flex items-center gap-3 cursor-pointer">
                        <input
                            type="checkbox"
                            name="saveChild"
                            checked={saveChild}
                            onChange={(e) => {
                                setSaveChild(e.target.checked);
                                setDirtySinceSubmit(true);
                            }}
                        />
                        <span className="font-medium">Save offspring</span>
                    </label>

                    <p className="text-sm text-gray-300">
                        *If unchecked, the birth event will still be recorded and used for inference.
                    </p>
                </div>

                {/* Optional Name */}
                {saveChild && (
                    <label className="flex flex-col">
                        <span className="font-medium">Offspring Name (optional)</span>
                        <input
                            name="name"
                            type="text"
                            placeholder="Enter name if saving"
                            className="bg-gray-800 border border-gray-500 rounded p-2"
                            value={childName}
                            onChange={(e) => {
                                setChildName(e.target.value);
                                setDirtySinceSubmit(true)
                            }}
                        />
                    </label>
                )}
                <button
                    type="submit"
                    className="mt-4 min-w-1/2 bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700"
                >
                    Record Child
                </button>
                {serverMessage && (
                    <p className="text-yellow-500 font-medium">{serverMessage}</p>
                )}
                <div id={`suggestions`} aria-live="polite" aria-atomic="true">
                    {suggestions &&
                        <div className="flex flex-col w-full">
                            <ul className="text-yellow-500 font-medium mt-2">Suggestions:</ul>
                            {suggestions.map((str: string) => (
                                <li key={str} className="text-yellow-500 font-medium ml-2">
                                    {str}
                                </li>
                            ))}
                        </div>
                    }
                </div>
            </div>

            {/* Genotypes */}
            <fieldset className="border border-gray-500 bg-gray-800 p-3 rounded-lg">
                <legend className="font-semibold">Genotypes</legend>
                {categories.map((c) => {
                    const validationGenotypeErrors = validationErrors?.genotypes?.[c]; // string[] | undefined
                    const constraintGenotypeViolation = constraintErrors?.genotypes?.[c]; // ExcessAlleleViolationDTO | undefined
                    return (
                    <div key={c} className="mb-2 bg-blue-900 border border-gray-500 rounded-lg">
                        <div className="w-full bg-blue-500 pl-4 py-1 rounded-t-lg">
                            <CategoryTag category={c} />
                        </div>
                        <div className="flex justify-around p-4 flex-wrap">
                            <label className="mx-2">
                                Phenotype:
                                <select
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
                                    className="ml-1 py-1 border border-gray-500 rounded bg-gray-800"
                                >
                                    <option value="">None</option>
                                    {grades.map((g) => (
                                        <option key={g} value={g}>
                                            {g}
                                        </option>
                                    ))}
                                </select>
                            </label>
                            <label className="mx-2">
                                Hidden Allele:
                                <select
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
                                    className="ml-1 py-1 border border-gray-500 rounded bg-gray-800"
                                >
                                    <option value="">None</option>
                                    {grades.map((g) => (
                                        <option key={g} value={g}>
                                            {g}
                                        </option>
                                    ))}
                                </select>
                            </label>
                        </div>
                        <div id={`genotypes-${c}-error`} aria-live="polite" aria-atomic="true">
                            {validationGenotypeErrors?.map((err) => (
                                <p key={err} className="ml-2 text-sm text-yellow-500">{err}</p>
                            ))}

                            {constraintGenotypeViolation && (
                                <div className="ml-2">
                                    <p className="text-sm text-yellow-500">
                                        Attempted to record: {constraintGenotypeViolation.attemptedAllele}
                                    </p>
                                    <p className="text-sm text-yellow-500">
                                        Possible alleles: {constraintGenotypeViolation.validAlleles.join(", ")}
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                )})}
            </fieldset>
            </div>
        </form>
    );
}

function isErrorState(state: ChildState): state is Extract<ChildState, { status: "error" }> {
    return state.status === "error";
}

function isValidationFailed(
    state: Extract<ChildState, { status: "error" }>
): state is Extract<ChildState, { status: "error"; error: "VALIDATION_FAILED" }> {
    return state.error === "VALIDATION_FAILED";
}

function isGeneticConstraint(
    state: Extract<ChildState, { status: "error" }>
): state is Extract<ChildState, { status: "error"; error: "GENETIC_CONSTRAINT_VIOLATION" }> {
    return state.error === "GENETIC_CONSTRAINT_VIOLATION";
}
