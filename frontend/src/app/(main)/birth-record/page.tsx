import { BirthRecordFilter } from '@/app/lib/definitions';
import BirthRecordList from '@/app/ui/relationship/birth-record-list';
import { Suspense } from "react";

export default async function BirthRecordPage({ searchParams }: { searchParams: Promise<BirthRecordFilter>; }) {
    const sp = await searchParams;

    return (
        <div>
            <h1 className="text-2xl">Birth Records</h1>
            <Suspense fallback={<p>Loading List...</p>}>
                <BirthRecordList filter={sp} />
            </Suspense>
        </div>
    );
}