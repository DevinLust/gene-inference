'use client';

import { Sheep, ALL_CATEGORIES } from "@/app/lib/definitions";
import { useState, useActionState, startTransition, useMemo } from "react";
import { updateSheep, UpdateSheepState } from "@/app/lib/actions";
import GenotypeFields from "@/app/ui/genotype-fields";
import { ControlledGenotypeMap } from "@/app/ui/genotype-fields";

type GenotypePatch = Partial<ControlledGenotypeMap>;

export default function EditSheepForm({ sheep }: { sheep: Sheep }) {
    const initialState: UpdateSheepState = { status: "idle" };
    const updateSheepForThisSheep = updateSheep.bind(null, sheep.id);
    const [state, formAction, isPending] = useActionState<UpdateSheepState, FormData>(
        updateSheepForThisSheep,
        initialState
    );

    const initialGenotypes = useMemo(() => createInitializedGenotypes(sheep), [sheep]);
    const [genotypes, setGenotypes] = useState<ControlledGenotypeMap>(initialGenotypes);
    const [name, setName] = useState(sheep.name ?? "");

    const changedName = buildChangedName(sheep.name, name);
    const genotypePatch = buildChangedGenotypePatch(initialGenotypes, genotypes);
    const isDirty = changedName !== null || genotypePatch !== null;

    const errorState = isErrorState(state) ? state : null;

    const validationErrors =
        errorState && isValidationFailed(errorState) ? errorState.errors : null;

    const constraintErrors =
        errorState && isGeneticConstraint(errorState) ? errorState.errors : null;

    return (
        <form
            onSubmit={(e) => {
                e.preventDefault();
                if (isPending || !isDirty) return;

                const formData = new FormData();

                if (changedName !== null) {
                    formData.set("name", changedName);
                }

                if (genotypePatch) {
                    for (const category of ALL_CATEGORIES) {
                        const patch = genotypePatch[category];
                        if (!patch) continue;

                        formData.set(
                            `genotypes.${category}.phenotype`,
                            patch.phenotype
                        );
                        formData.set(
                            `genotypes.${category}.hiddenAllele`,
                            patch.hiddenAllele
                        );
                    }
                }

                startTransition(() => {
                    formAction(formData);
                });
            }}
            className="flex flex-col gap-6 p-4 max-w-lg bg-gray-600 rounded-lg"
        >
            {errorState?.message && <ServerResponse state={state} />}

            <label className="flex flex-col">
                <span className="font-medium">Sheep Name</span>
                <input
                    name="name"
                    type="text"
                    placeholder="Enter sheep name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="border border-gray-500 rounded p-2 bg-gray-800"
                />
            </label>

            <GenotypeFields
                genotypes={genotypes}
                setGenotypes={setGenotypes}
                lockedCategories={sheep.lockedCategories}
                validationErrors={validationErrors?.genotypes}
                constraintViolations={constraintErrors?.genotypes}
            />

            <button
                type="submit"
                disabled={isPending || !isDirty}
                className="flex gap-1 bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
                {isPending && <Spinner />}
                {isPending ? "Updating..." : "Update Sheep"}
            </button>

            {errorState?.message && <ServerResponse state={state} />}
        </form>
    );
}

function ServerResponse({ state }: { state: UpdateSheepState }) {
    if (state.status === "error") {
        return state.message ? (
            <p className="text-yellow-500 font-medium">{state.message}</p>
        ) : null;
    }

    if (state.status === "success") {
        return state.message ? (
            <p className="text-green-400 font-medium">{state.message}</p>
        ) : null;
    }

    return null;
}

function Spinner() {
    return (
        <svg
            className="h-4 w-4 animate-spin text-white"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            aria-label="Loading"
        >
            <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
            />
            <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
            />
        </svg>
    );
}

function createInitializedGenotypes(sheep: Sheep): ControlledGenotypeMap {
    return ALL_CATEGORIES.reduce((acc, cur) => {
        acc[cur] = {
            phenotype: sheep.genotypes[cur]?.phenotype ?? "",
            hiddenAllele: sheep.genotypes[cur]?.hiddenAllele ?? "",
        };
        return acc;
    }, {} as ControlledGenotypeMap);
}

function normalizeValue(value: string): string | null {
    const trimmed = value.trim();
    return trimmed === "" ? null : trimmed;
}

function sameValue(a: string, b: string): boolean {
    return normalizeValue(a) === normalizeValue(b);
}

function buildChangedName(initialName: string | null, currentName: string): string | null {
    const normalizedInitial = (initialName ?? "").trim();
    const normalizedCurrent = currentName.trim();

    return normalizedInitial === normalizedCurrent ? null : normalizedCurrent;
}

function buildChangedGenotypePatch(
    initial: ControlledGenotypeMap,
    current: ControlledGenotypeMap
): GenotypePatch | null {
    const patch: GenotypePatch = {};

    for (const category of ALL_CATEGORIES) {
        const initialPhenotype = initial[category].phenotype;
        const initialHidden = initial[category].hiddenAllele;

        const currentPhenotype = current[category].phenotype;
        const currentHidden = current[category].hiddenAllele;

        const phenotypeChanged = !sameValue(initialPhenotype, currentPhenotype);
        const hiddenChanged = !sameValue(initialHidden, currentHidden);

        if (!phenotypeChanged && !hiddenChanged) {
            continue;
        }

        patch[category] = {
            phenotype: phenotypeChanged ? currentPhenotype : "",
            hiddenAllele: hiddenChanged ? currentHidden : "",
        };
    }

    return Object.keys(patch).length > 0 ? patch : null;
}

function isErrorState(state: UpdateSheepState): state is Extract<UpdateSheepState, { status: "error" }> {
    return state.status === "error";
}

function isValidationFailed(
    state: Extract<UpdateSheepState, { status: "error" }>
): state is Extract<UpdateSheepState, { status: "error"; error: "VALIDATION_FAILED" }> {
    return state.error === "VALIDATION_FAILED";
}

function isGeneticConstraint(
    state: Extract<UpdateSheepState, { status: "error" }>
): state is Extract<UpdateSheepState, { status: "error"; error: "GENETIC_CONSTRAINT_VIOLATION" }> {
    return state.error === "GENETIC_CONSTRAINT_VIOLATION";
}
