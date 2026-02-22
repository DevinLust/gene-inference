import { fetchSheepById } from "@/app/lib/data";
import { notFound } from 'next/navigation';
import EditableSheepName from '@/app/ui/sheep/EditableSheepName';
import GenotypeTable from '@/app/ui/sheep/genotype-table';
import DistributionTable from '@/app/ui/sheep/distribution-table';
import Link from 'next/link';

export default async function SheepDetailPage(props: { params: Promise<{ id: string }> }) {
    const params = await props.params;
    const id = params.id;

    const sheep = await fetchSheepById(id);

    if (!sheep) {
        notFound();
    }

    return (
        <div>
            <Link href="/sheep" className="text-blue-400 mb-8">Back to Sheep List</Link>

            {/* Name */}
            <EditableSheepName sheepId={sheep.id} initialName={sheep.name ?? ""} />

            {/* ID */}
            <p className="mb-2">ID: {sheep.id}</p>

            {/* Parent Relationship ID */}
            <p className="mt-2">Parent Relationship ID: {sheep.parentRelationshipId ?? "no registered parents"}</p>

            {/* Genotypes */}
            <GenotypeTable sheep={sheep} />

            {/* Distributions */}
            <DistributionTable sheep={sheep} />
        </div>
    );
}
