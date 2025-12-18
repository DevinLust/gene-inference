'use client';

import { useActionState } from "react";
import { breedSheep } from "@/app/lib/actions";

export default function BreedForm() {
    const [state, formAction] = useActionState(breedSheep, { message: "" });

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
                    type="text"
                    placeholder="Enter first parent ID"
                    className="bg-gray-800 border border-gray-500 rounded p-2"
                />
            </label>

            {/* Parent 2 */}
            <label className="flex flex-col">
                <span className="font-medium">Parent 2 ID</span>
                <input
                    name="parent2Id"
                    type="text"
                    placeholder="Enter second parent ID"
                    className="bg-gray-800 border border-gray-500 rounded p-2"
                />
            </label>

            {/* Submit */}
            <button
                type="submit"
                className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700"
            >
                Breed Sheep
            </button>

            {/* Server response */}
            {state?.message && (
                <p className="text-green-600 font-medium">
                    {state.message}
                </p>
            )}
        </form>
    );
}
