"use client";

import { useState } from "react";

export default function SheepIdPicker({
                                  name,
                                  sheep,
                                  placeholder,
                              }: {
    name: string; // "parent1Id" or "parent2Id"
    sheep: { id: number; name: string | null }[];
    placeholder?: string;
}) {
    const [value, setValue] = useState("");

    return (
        <div className="flex flex-col gap-1">
            <input
                list={`${name}-list`}
                value={value}
                onChange={(e) => setValue(e.target.value)}
                placeholder={placeholder ?? "Search sheep by name…"}
                className="bg-gray-800 border border-gray-500 rounded p-2"
            />

            <datalist id={`${name}-list`}>
                {sheep.map((s) => (
                    <option
                        key={s.id}
                        value={String(s.id)}
                        label={s.name ?? "(unnamed)"}
                    />
                ))}
            </datalist>

            {/* Hidden field becomes unnecessary because the input value IS the id */}
            <input type="hidden" name={name} value={value} />
        </div>
    );
}
