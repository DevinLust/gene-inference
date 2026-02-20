import { fetchRelationshipById } from "@/app/lib/data";
import CategoryCard from "@/app/ui/category-card";
import { Category, Relationship, PhenotypeFrequencies } from "@/app/lib/definitions";
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
            <Link href="/sheep/relationship" className="inline-block text-blue-400 mb-4">Back to Relationship List</Link>

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
            <div className="mt-6 mb-4 max-w-sm">
                <h2 className="mb-2 text-xl">Phenotype Frequencies</h2>
                <div className="grid grid-cols-1 gap-2">
                    {Object.entries(relationship.phenotypeFrequencies).map(([cat, epochMap]) => (
                        <CategoryCard category={cat as Category} key={cat}>
                            <ul>
                                {Object.entries(epochMap).map(([pair, freqMap]) => (
                                    <li key={pair}>
                                        <span>{pair}:</span>
                                        <div className="flex flex-wrap gap-2 justify-between">
                                            {Object.entries(freqMap).map(([grade, frequency]) => (
                                                <p key={grade}>{grade}: {frequency}</p>
                                            ))}
                                        </div>
                                    </li>

                                    ))}
                            </ul>
                        </CategoryCard>
                    ))}
                </div>
            </div>
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
