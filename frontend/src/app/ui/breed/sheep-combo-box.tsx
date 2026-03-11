"use client";

import { useMemo, useState, useRef, useEffect } from "react";
import { SheepSummary } from "@/app/lib/definitions";

type ComboboxInput = {
    inputLabel: string;
    sheep: SheepSummary[];
    fieldName?: string;

    // controlled value (optional)
    selectedId?: number | null;

    // notify parent (optional)
    onSelect?: (id: number | null) => void;
};

export default function SheepCombobox({
                                          inputLabel,
                                          sheep,
                                          fieldName,
                                          selectedId,
                                          onSelect,
                                      }: ComboboxInput) {
    const [query, setQuery] = useState("");
    const [internalSelected, setInternalSelected] = useState<SheepSummary | null>(null);
    const [open, setOpen] = useState(false);

    const containerRef = useRef<HTMLDivElement>(null);

    // click-outside handler
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // decide controlled vs uncontrolled
    const isControlled = selectedId !== undefined;

    // compute selected sheep based on controlled id (if present)
    const controlledSelected = useMemo(() => {
        if (!isControlled) return null;
        if (selectedId == null) return null;
        return sheep.find((s) => s.id === selectedId) ?? null;
    }, [isControlled, selectedId, sheep]);

    const selected = isControlled ? controlledSelected : internalSelected;

    function setSelected(next: SheepSummary | null) {
        if (!isControlled) {
            setInternalSelected(next);
        }
        onSelect?.(next?.id ?? null);
    }

    const filtered = useMemo(() => {
        const q = query.trim().toLowerCase();
        if (!q) return sheep.slice(0, 24);

        return sheep
            .filter(
                (s) =>
                    (s.name ?? "").toLowerCase().includes(q) ||
                    String(s.id).includes(q)
            )
            .slice(0, 24);
    }, [query, sheep]);

    return (
        <div className="flex flex-col gap-1">
            <span className="font-medium">{inputLabel}</span>

            {/* what backend receives */}
            {fieldName && (
                <input type="hidden" name={fieldName} value={selected?.id ?? ""} />
            )}

            <div className="relative flex items-start gap-2">
                {/* Search input */}
                <div className="flex-1 relative" ref={containerRef}>
                    <input
                        value={query}
                        onChange={(e) => {
                            setQuery(e.target.value);
                            setOpen(true);
                        }}
                        onFocus={() => setOpen(true)}
                        placeholder={selected ? "Change selection…" : "Search by name or id…"}
                        className="w-full bg-gray-800 border border-gray-500 rounded p-2"
                    />

                    {open && (
                        <div className="absolute top-full mt-1 w-full rounded border border-gray-600 bg-gray-900 shadow-lg z-50 max-h-56 overflow-auto">
                            {filtered.length === 0 ? (
                                <div className="px-3 py-2 text-sm text-gray-400">No matches</div>
                            ) : (
                                filtered.map((s) => (
                                    <button
                                        key={s.id}
                                        type="button"
                                        onClick={() => {
                                            setSelected(s);
                                            setOpen(false);
                                            setQuery(""); // clear search after selection
                                        }}
                                        className="w-full text-left px-3 py-2 hover:bg-white/10"
                                    >
                                        <div className="font-medium">{s.name ?? "(unnamed)"}</div>
                                        <div className="text-xs text-gray-400">ID: {s.id}</div>
                                    </button>
                                ))
                            )}
                        </div>
                    )}
                </div>

                {/* Selected sheep box */}
                <div className="min-w-[180px] max-w-[240px] rounded border border-gray-600 bg-gray-800 px-3 py-2">
                    <div className="text-xs text-gray-300">Selected</div>

                    {selected ? (
                        <div className="mt-1">
                            <div className="font-medium truncate">{selected.name ?? "(unnamed)"}</div>
                            <div className="text-xs text-gray-400">ID: {selected.id}</div>

                            <button
                                type="button"
                                onClick={() => setSelected(null)}
                                className="mt-2 text-xs text-red-300 hover:text-red-200"
                            >
                                Clear
                            </button>
                        </div>
                    ) : (
                        <div className="mt-1 text-sm text-gray-400">None</div>
                    )}
                </div>
            </div>
        </div>
    );
}
