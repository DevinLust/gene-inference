'use client';

import { SheepSummary, Category } from '@/app/lib/definitions';
import { useActionState, useState, useEffect, startTransition } from "react";
import { breedSheep } from "@/app/lib/actions";
import SheepComboBox from './sheep-combo-box';
import CategoryTag from '@/app/ui/category-tag';
import { useBreedSheep } from "@/app/(main)/breed/breed-sheep-provider";

export default function BreedForm() {
    const sheep: SheepSummary[] = useBreedSheep();
    const [state, formAction, isPending] = useActionState(breedSheep, { message: "", errors: {} });

    const [saveChild, setSaveChild] = useState(false);

    // controlled selections
    const [parent1Id, setParent1Id] = useState<number | null>(null);
    const [parent2Id, setParent2Id] = useState<number | null>(null);

    // child name (optional)
    const [childName, setChildName] = useState<string>("");

    // hide old server errors once user changes inputs
    const [dirtySinceSubmit, setDirtySinceSubmit] = useState(false);

    useEffect(() => {
        // whenever the server returns a new state,
        // we are no longer "dirty since submit"
        setDirtySinceSubmit(false);
    }, [state]);

    const showServerErrors = !dirtySinceSubmit;

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
            className="flex flex-col gap-6 p-4 max-w-md bg-gray-600 rounded-lg"
        >
            {(showServerErrors &&
                (state?.errors?.parent1MissingCategories || state?.errors?.parent2MissingCategories)) && (
                <p className="text-yellow-500 font-medium">
                    To breed these sheep, you&apos;ll have to record an external event through Record Child
                </p>
            )}

            <h2 className="text-lg font-semibold">Breed Sheep</h2>

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
                    {showServerErrors && state.errors?.parent1MissingCategories?.length > 0 && (
                        <div className="flex flex-col w-full">
                            <p className="mt-2 text-sm text-yellow-500">Missing hidden alleles in these categories</p>
                            <div className="flex gap-2">
                                {state.errors.parent1MissingCategories.map((error: string) => (
                                    <CategoryTag category={error as Category} key={error} />
                                ))}
                            </div>
                        </div>
                    )}
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
                    {showServerErrors && state.errors?.parent2MissingCategories?.length > 0 && (
                        <div className="flex flex-col w-full">
                            <p className="mt-2 text-sm text-yellow-500">Missing hidden alleles in these categories</p>
                            <div className="flex gap-2">
                                {state.errors.parent2MissingCategories.map((error: string) => (
                                    <CategoryTag category={error as Category} key={error} />
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Save child checkbox */}
            <div className="flex flex-col gap-1">
                <label className="flex items-center gap-3 cursor-pointer">
                    <input
                        type="checkbox"
                        name="saveChild"
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
                className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700"
            >
                Breed Sheep
            </button>

            {showServerErrors && state?.message && state?.errors == null && (
                <p className="text-yellow-500 font-medium">{state.message}</p>
            )}
        </form>
    );
}
