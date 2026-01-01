'use client';

import { useActionState, useState, startTransition } from "react";
import { createSheep, CreateState } from "@/app/lib/actions";
import DistributionForm from "./distributions-subform";
import CategoryTag from "@/app/ui/category-tag";
import FeatureInProgress from "@/app/ui/in-progress";
import type { Category, Grade } from "@/app/lib/definitions";

const categories: Category[] = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];
const grades: Grade[] = ["S", "A", "B", "C", "D", "E"];

export default function SheepForm() {
    // Hook into the server action
    const initialState: CreateState = { message: null, errors: {} }
    const [state, formAction, isPending] = useActionState(createSheep, initialState);

    const [name, setName] = useState("");

    const [genotypes, setGenotypes] = useState(() =>
        categories.reduce((acc, c) => {
            acc[c] = { phenotype: "", hiddenAllele: "" };
            return acc;
        }, {} as Record<string, { phenotype: string; hiddenAllele: string }>)
    );

    const [parentRelationshipId, setParentRelationshipId] = useState("");

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
            className="flex flex-col gap-6 p-4 max-w-lg bg-gray-600 rounded-lg"
        >
            {/* Show server response */}
            {state?.message && <ServerResponse state={state} />}

            {/* Name */}
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

            {/* Genotypes */}
            <fieldset className="border border-gray-500 bg-gray-800 p-3 rounded-lg">
                <legend className="font-semibold">Genotypes</legend>
                {categories.map((c) => (
                    <div key={c} className="mb-2 bg-blue-900 border border-gray-500 rounded-lg">
                        <div className="w-full bg-blue-500 pl-4 py-1 rounded-t-lg">
                            <CategoryTag category={c} />
                        </div>
                        <div className="flex justify-around p-4">
                            <label>
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
                            <label>
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
                    </div>
                ))}

                <div id="genotypes-error" aria-live="polite" aria-atomic="true">
                    {state.errors?.genotypes &&
                        state.errors.genotypes.map((error: string) => (
                            <p className="mt-2 text-sm text-red-500" key={error}>
                                {error}
                            </p>
                        ))}
                </div>
            </fieldset>

            {/* Distributions */}
            <FeatureInProgress>
                <DistributionForm />
            </FeatureInProgress>

            {/* Parent Relationship */}
            <FeatureInProgress>
                <label className="flex flex-col">
                    <span className="font-medium">Parent Relationship ID</span>
                    <input
                        name="parentRelationshipId"
                        type="number"
                        value={parentRelationshipId}
                        onChange={(e) => setParentRelationshipId(e.target.value)}
                        placeholder="Optional: Enter relationship ID of parents"
                        className="bg-gray-800 border border-gray-500 rounded p-2"
                    />
                </label>
            </FeatureInProgress>

            {/* Submit button */}
            <button
                type="submit"
                disabled={isPending}
                className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700"
            >
                {isPending && <Spinner />}
                {isPending ? "Creating..." : "Create Sheep"}
            </button>

            {/* Show server response */}
            {state?.message && <ServerResponse state={state} />}
        </form>
    );
}

function ServerResponse({ state }: { state: CreateState }) {
    return <p className="text-red-500 font-medium">{state.message}</p>;
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

