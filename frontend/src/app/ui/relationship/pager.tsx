import { BirthRecordRow, BirthRecordFilter, PageResponse } from '@/app/lib/definitions';
import { buildQuery } from '@/app/lib/helpers';
import Link from "next/link";

export default function Pager({ page, filter }: { page: PageResponse<BirthRecordRow>; filter: BirthRecordFilter }) {
    const curr = page.page ?? 0;
    const totalPages = page.totalPages ?? 1;

    const base = buildQuery(filter);

    const hrefFor = (p: number) => {
        const sp = new URLSearchParams(base);
        sp.set("page", String(p));
        return `?${sp.toString()}`;
    };

    return (
        <div className="mt-3 flex items-center justify-between px-2 text-sm">
            <div className="text-gray-300">
                Showing <span className="tabular-nums">{page.items.length}</span> of{" "}
                <span className="tabular-nums">{page.totalElements}</span>
            </div>

            <div className="flex items-center gap-2">
                <Link
                    href={hrefFor(Math.max(0, curr - 1))}
                    className={`rounded border border-gray-500 px-3 py-1 hover:bg-gray-700 ${
                        curr === 0 ? "pointer-events-none opacity-50" : ""
                    }`}
                >
                    Prev
                </Link>

                <span className="text-gray-300 tabular-nums">
          Page {curr + 1} / {Math.max(1, totalPages)}
        </span>

                <Link
                    href={hrefFor(Math.min(totalPages - 1, curr + 1))}
                    className={`rounded border border-gray-500 px-3 py-1 hover:bg-gray-700 ${
                        page.hasNext ? "" : "pointer-events-none opacity-50"
                    }`}
                >
                    Next
                </Link>
            </div>
        </div>
    );
}
