import { fetchSheepById } from "@/app/lib/data";
import { notFound } from 'next/navigation';
import EditableSheepName from '@/app/ui/sheep/editable-sheep-name';
import GenotypeTables from '@/app/ui/sheep/genotype-tables';
import DistributionTable from '@/app/ui/sheep/distribution-table';
import SheepEditButton from '@/app/ui/sheep/sheep-edit-button';
import SheepDeleteButton from '@/app/ui/sheep/sheep-delete-button';
import GraphBackButton from '@/app/ui/graph-back-button';
import { RelationshipGraphLink } from '@/app/ui/buttons';

export default async function SheepDetailPage(props: { params: Promise<{ id: string }> }) {
    const params = await props.params;
    const id = params.id;

    const sheep = await fetchSheepById(id);

    if (!sheep) {
        notFound();
    }

    return (
        <div>
            <GraphBackButton fallbackHref={"/sheep"} />

            {/* Name */}
            <div className="flex flex-wrap justify-between">
                <EditableSheepName sheepId={sheep.id} initialName={sheep.name ?? ""} />
                <div className="flex gap-2">
                    <SheepEditButton sheepId={sheep.id} />
                    <SheepDeleteButton sheepId={sheep.id} />
                </div>
            </div>


            {/* ID */}
            <p className="mb-2">ID: {sheep.id}</p>

            {/* Parent Relationship ID */}
            <p className="my-2">
                Parent Relationship:
                { sheep.parentRelationshipId ?
                    <RelationshipGraphLink relId={sheep.parentRelationshipId} />
                    :
                    <span className="text-gray-400"> (no registered parents)</span>
                }
            </p>

            {/* Genotypes */}
            <GenotypeTables sheep={sheep} />

            {/* Distributions */}
            <DistributionTable sheep={sheep} />
        </div>
    );
}
