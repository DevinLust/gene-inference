'use client';

import { useActionState, useState } from "react";
import { breedSheep } from "@/app/lib/actions";

export default function BreedForm() {
    const [state, formAction] = useActionState(breedSheep, { message: "", errors: {} });

    const [sheep1Id, setSheep1Id] = useState("");

    const [sheep2Id, setSheep2Id] = useState("");

    return (
        <form
            action={formAction}
            className="flex flex-col gap-6 p-4 max-w-md bg-gray-600 rounded-lg"
        >
            <h2 className="text-lg font-semibold">Breed Sheep</h2>

            {/* Parent 1 */}
            <label className="flex flex-col">
                <span className="font-medium">Parent 1 ID</span>
                <input
                    name="parent1Id"
                    type="number"
                    min={1}
                    value={sheep1Id}
                    onChange={(e) => setSheep1Id(e.target.value)}
                    placeholder="Enter first parent ID"
                    className="bg-gray-800 border border-gray-500 rounded p-2"
                />
                <div id="sheep1Id-error" aria-live="polite" aria-atomic="true">
                    {state.errors?.sheep1Id && <p className="mt-2 text-sm text-red-500">{state.errors.sheep1Id}</p>}
                </div>
            </label>


            {/* Parent 2 */}
            <label className="flex flex-col">
                <span className="font-medium">Parent 2 ID</span>
                <input
                    name="parent2Id"
                    type="number"
                    min={1}
                    value={sheep2Id}
                    onChange={(e) => setSheep2Id(e.target.value)}
                    placeholder="Enter second parent ID"
                    className="bg-gray-800 border border-gray-500 rounded p-2"
                />
                <div id="sheep2Id-error" aria-live="polite" aria-atomic="true">
                    {state.errors?.sheep2Id && <p className="mt-2 text-sm text-red-500">{state.errors.sheep2Id}</p>}
                </div>
            </label>

            {/* Submit */}
            <button
                type="submit"
                className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700"
            >
                Breed Sheep
            </button>

            {/* Server response */}
            {state?.message && <p className="text-red-500 font-medium">{state.message}</p>}
        </form>
    );
}
