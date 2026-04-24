'use client';

import { useActionState, useState, useEffect, startTransition } from 'react';
import { recordChild, ChildState } from '@/app/lib/actions';
import { SheepSummary } from '@/app/lib/definitions';
import SheepComboBox from "./sheep-combo-box";
import { useBreedSheep } from "@/app/(main)/breed/breed-sheep-provider";
import { useSearchParams } from "next/navigation";
import GenotypeFields from "@/app/ui/genotype-fields";
import { ControlledGenotypeMap, createEmptyControlledGenotypes } from "@/app/ui/genotype-fields";

export default function RecordChildForm() {
    const sheep: SheepSummary[] = useBreedSheep();
    const initialState: ChildState = { status: "idle" };
    const [state, formAction, isPending] = useActionState(recordChild, initialState);

    const [saveChild, setSaveChild] = useState(false);

    // controlled selections
    const searchParams = useSearchParams();

    const parseId = (value: string | null): number | null => {
        if (!value) return null;
        const n = Number(value);
        return Number.isFinite(n) ? n : null;
    };

    const [parent1Id, setParent1Id] = useState<number | null>(parseId(searchParams.get("parent1Id")));
    const [parent2Id, setParent2Id] = useState<number | null>(parseId(searchParams.get("parent2Id")));

    // child name (optional)
    const [childName, setChildName] = useState<string>("");

    // controlled genotypes
    const [genotypes, setGenotypes] = useState<ControlledGenotypeMap>(createEmptyControlledGenotypes);

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
            className="flex flex-col gap-6 p-4 bg-gray-600 rounded-lg w-full max-w-5xl"
        >
            <h2 className="text-lg font-semibold">Record External Event</h2>

            <div className="grid grid-cols-1 gap-4 lg:grid-cols-2 items-start">
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
                    disabled={isPending}
                    className={`mt-4 min-w-1/2 rounded px-4 py-2 text-white flex items-center justify-center gap-2 transition
        ${isPending
                        ? "bg-blue-400 cursor-not-allowed opacity-70"
                        : "bg-blue-600 hover:bg-blue-700"
                    }`}
                >
                    {isPending && (
                        <svg
                            className="h-4 w-4 animate-spin"
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                            aria-hidden="true"
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
                    )}
                    <span>{isPending ? "Recording..." : "Record Child"}</span>
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
                <div className="min-w-0 w-full">
                    <GenotypeFields
                        genotypes={genotypes}
                        setGenotypes={setGenotypes}
                        validationErrors={validationErrors?.genotypes}
                        constraintViolations={constraintErrors?.genotypes}
                    />
                </div>
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
