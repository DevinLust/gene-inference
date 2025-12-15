import { fetchRelationshipById } from "@/app/lib/data";
import CategoryCard from "@/app/ui/category-card";
import { Category, Relationship, Grade } from "@/app/lib/definitions";
import { notFound } from 'next/navigation';
import { SheepDetails } from '@/app/ui/sheep/buttons';
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
            <Link href="/sheep/relationship" className="text-blue-400 mb-8">Back to Relationship List</Link>

            {/* ID */}
            <p className="mb-2">ID: {relationship.id}</p>

            {/* Parent 1 */}
            <p className="mt-2">Parent 1: <SheepDetails sheepId={relationship.parent1.id}>{relationship.parent1.name}</SheepDetails></p>

            {/* Parent 2 */}
            <p className="mt-2">Parent 2: <SheepDetails sheepId={relationship.parent2.id}>{relationship.parent2.name}</SheepDetails></p>

            {/* Total children */}
            <p className="mt-2">Total Children: {totalChildren(relationship.phenotypeFrequencies)}</p>

            {/* Phenotype Frequency */}
            <div className="mt-6 mb-4 max-w-sm">
                <h2 className="mb-2 text-xl">Phenotype Frequencies</h2>
                <div className="grid grid-cols-1 gap-2">
                    {Object.entries(relationship.phenotypeFrequencies).map(([cat, freqMap]) => (
                        <CategoryCard category={cat as Category} key={cat}>
                            <ul>
                                {Object.entries(freqMap).map(([grade, frequency]) => (
                                    <li key={grade}>{grade}: {frequency}</li>
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
        Record<Category, Partial<Record<Grade, number>>>
    ): number {
        const categories = Object.values(phenotypeFrequencies);

        if (categories.length === 0) return 0;

        return Object.values(categories[0]).reduce(
            (sum, count) => sum + (count ?? 0),
            0
        );
    }
