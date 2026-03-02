import { fetchRelationshipById } from "@/app/lib/data";
import { Relationship, PhenotypeFrequencies } from "@/app/lib/definitions";
import { notFound } from 'next/navigation';
import { SheepDetails } from '@/app/ui/buttons';
import PhenotypeFrequencyTable from "@/app/ui/relationship/phenotype-frequency-table";
import Link from 'next/link';

export default async function RelationshipDetailPage(props: { params: Promise<{ id: string }> }) {
    const params = await props.params;
    const id = params.id;

    const relationship: Relationship = await fetchRelationshipById(id);

    if (!relationship) {
        notFound();
    }

    return (
        <div>
            <Link href="/relationship" className="inline-block text-blue-400 mb-4">Back to Relationship List</Link>

            {/* ID */}
            <p className="mb-2">ID: {relationship.id}</p>

            {/* Parent 1 */}
            <p className="mt-2">Parent 1: <SheepDetails sheepId={relationship.parent1.id}>{relationship.parent1.name}</SheepDetails></p>

            {/* Parent 2 */}
            <p className="mt-2">Parent 2: <SheepDetails sheepId={relationship.parent2.id}>{relationship.parent2.name}</SheepDetails></p>

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
