import { BirthRecordFilter } from '@/app/lib/definitions';
import BirthRecordList from '@/app/ui/relationship/birth-record-list';

export default async function BirthRecordPage({ searchParams }: { searchParams: Promise<BirthRecordFilter>; }) {
    const sp = await searchParams;

    return (
        <div>
            <h1 className="text-2xl">Birth Records</h1>
            <BirthRecordList filter={sp} />
        </div>
    );
}