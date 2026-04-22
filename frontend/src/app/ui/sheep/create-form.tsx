'use client';

import { useActionState, useState, startTransition } from "react";
import { createSheep, CreateState } from "@/app/lib/actions";
import DistributionForm from "./distributions-subform";
import FeatureInProgress from "@/app/ui/in-progress";
import GenotypeFields from "@/app/ui/genotype-fields";
import { ControlledGenotypeMap, createEmptyControlledGenotypes } from "@/app/ui/genotype-fields";

export default function SheepForm() {
    // Hook into the server action
    const initialState: CreateState = { message: null, errors: {} }
    const [state, formAction, isPending] = useActionState(createSheep, initialState);

    const [name, setName] = useState("");

    const [genotypes, setGenotypes] = useState<ControlledGenotypeMap>(createEmptyControlledGenotypes);

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
            <GenotypeFields
                genotypes={genotypes}
                setGenotypes={setGenotypes}
                validationErrors={state.errors?.genotypes}
            />

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
    return <p className="text-yellow-500 font-medium">{state.message}</p>;
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

