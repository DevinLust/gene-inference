import { fetchSheepById } from "@/app/lib/data";
import { notFound } from 'next/navigation';
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
            <h1 className="mt-2 mb-2 text-2xl text-bold"><span>{sheep.name || <span className="text-gray-400">(unnamed)</span>}</span></h1>

            {/* ID */}
            <p className="mb-2">ID: {sheep.id}</p>

            {/* Parent Relationship ID */}
            <p className="mt-2">Parent Relationship ID: {sheep.parentRelationshipId ?? "no registered parents"}</p>

            {/* Genotypes */}
            <div className="mt-6 mb-4">
                <h2 className="mb-2 text-xl">Genotypes</h2>
                {Object.entries(sheep.genotypes).map(([cat, genotype]) => (
                    <div key={cat} className="mb-2 border-t border-b border-white p-4">
                        <h3>{cat}:</h3>
                        <ul>
                            <li key={`${cat}.phenotype`}>phenotype: {genotype.phenotype}</li>
                            <li key={`${cat}.hiddenAllele`}>hidden allele: {genotype.hiddenAllele ?? "unknown"}</li>
                        </ul>
                    </div>
                ))}
            </div>

            {/* Distributions */}
            <div className="mt-6 mb-4">
                <h2 className="mb-1 text-xl">Distributions:</h2>
                {Object.entries(sheep.distributions).map(([cat, distribution]) => (
                    <div key={`${cat}`} className="mt-4 border border-white p-4">
                        <h2 className="mt-1 mb-1">{cat}:</h2>
                        <h3>INFERRED</h3>
                        <div key={`${cat}.INFERRED`} className="grid grid-cols-6 gap-2 mb-2">
                            {Object.entries(distribution.INFERRED).map(([grade, prob]) => (
                                <p key={`${cat}.INFERRED.${grade}`}>{grade}: {(prob * 100).toFixed(2)}%</p>
                            ))}
                        </div>

                        <h3 className="mt-1">PRIOR</h3>
                        <div key={`${cat}.PRIOR`} className="grid grid-cols-6 gap-2 mb-2">
                            {Object.entries(distribution.PRIOR).map(([grade, prob]) => (
                                <p key={`${cat}.PRIOR.${grade}`}>{grade}: {(prob * 100).toFixed(2)}%</p>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
