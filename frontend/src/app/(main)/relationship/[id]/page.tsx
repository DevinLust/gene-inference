import { fetchRelationshipById } from "@/app/lib/data";
import { Relationship, PhenotypeFrequencies } from "@/app/lib/definitions";
import { notFound } from 'next/navigation';
import PhenotypeFrequencyTable from "@/app/ui/relationship/phenotype-frequency-table";
import GraphBackButton from '@/app/ui/graph-back-button';
import { SheepGraphLink, BreedSheepButton, RecordChildButton } from '@/app/ui/buttons';

export default async function RelationshipDetailPage(props: { params: Promise<{ id: string }> }) {
    const params = await props.params;
    const id = params.id;

    const relationship: Relationship = await fetchRelationshipById(id);

    if (!relationship) {
        notFound();
    }

    return (
        <div>
            <GraphBackButton fallbackHref={"/relationship"} />

            {/* ID */}
            <div className="flex flex-wrap justify-between mb-2">
                <p>ID: {relationship.id}</p>
                <div className="flex flex-wrap justify-start gap-2">
                    <BreedSheepButton parent1Id={relationship.parent1.id} parent2Id={relationship.parent2.id} label="Breed More +" />
                    <RecordChildButton parent1Id={relationship.parent1.id} parent2Id={relationship.parent2.id} label="Record Another +" />
                </div>
            </div>

            {/* Parent 1 */}
            <p className="mt-2">Parent 1: <SheepGraphLink sheepId={relationship.parent1.id}>{relationship.parent1.name}</SheepGraphLink></p>

            {/* Parent 2 */}
            <p className="mt-2">Parent 2: <SheepGraphLink sheepId={relationship.parent2.id}>{relationship.parent2.name}</SheepGraphLink></p>

            {/* Total children */}
            <p className="mt-2">Total Children: {totalChildren(relationship.phenotypeFrequencies)}</p>

            {/* Phenotype Frequency */}
            <PhenotypeFrequencyTable relationship={relationship} />
        </div>
    );
}

function totalChildren(phenotypeFrequencies: 
        PhenotypeFrequencies
    ): number {
        const byPair = Object.values(phenotypeFrequencies)[0]; // Record<GradePairKey, Partial<Record<Grade, number>>>

        let total = 0;

        for (const childrenMap of Object.values(byPair)) {
            for (const count of Object.values(childrenMap)) {
                // count is number | undefined (because Partial<Record<...>>)
                if (typeof count === "number") total += count;
            }
        }

        return total;
    }
