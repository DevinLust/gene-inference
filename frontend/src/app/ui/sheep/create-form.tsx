'use client';

import { useActionState } from "react";
import { createSheep } from "@/app/lib/actions";
import DistributionForm from "./distributions-subform";
import CategoryTag from "@/app/ui/category-tag";
import type { Category, Grade } from "@/app/lib/definitions";

const categories: Category[] = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];
const grades: Grade[] = ["S", "A", "B", "C", "D", "E"];

export default function SheepForm() {
    // Hook into the server action
    const [state, formAction] = useActionState(createSheep, { message: "" });

    return (
        <form action={formAction} className="flex flex-col gap-6 p-4 max-w-lg bg-gray-600 rounded-lg">
            {/* Name */}
            <label className="flex flex-col">
                <span className="font-medium">Sheep Name</span>
                <input
                    name="name"
                    type="text"
                    placeholder="Enter sheep name"
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
            </fieldset>

            {/* Distributions */}
            <DistributionForm />

            {/* Parent Relationship */}
            <label className="flex flex-col">
                <span className="font-medium">Parent Relationship ID</span>
                <input
                    name="parentRelationshipId"
                    type="text"
                    placeholder="Optional: Enter relationship ID of parents"
                    className="bg-gray-800 border border-gray-500 rounded p-2"
                />
            </label>

            {/* Submit button */}
            <button
                type="submit"
                className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700"
            >
                Create Sheep
            </button>

            {/* Show server response */}
            {state?.message && (
                <p className="text-green-600 font-medium">{state.message}</p>
            )}
        </form>
    );
}
