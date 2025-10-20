import { PhenotypeDistributions, Category, Grade } from '@/app/lib/definitions';

export default function PhenotypeDistributionsList({ phenotypeDistributions }: { phenotypeDistributions: PhenotypeDistributions }) {
    return (
        <div className="mt-6 mb-4">
            <h2 className="mb-1 text-xl">Distributions:</h2>
            {Object.entries(phenotypeDistributions).map(([cat, distribution]) => (
                <div key={`${cat}`} className="mt-4 border border-white p-4">
                    <h2 className="mt-1 mb-1">{cat}:</h2>
                    <div key={`${cat}.INFERRED`} className="grid grid-cols-6 gap-2 mb-2">
                        {Object.entries(distribution).map(([grade, prob]) => (
                            <p key={`${cat}.INFERRED.${grade}`}>{grade}: {(prob * 100).toFixed(2)}%</p>
                        ))}
                    </div>
                </div>
            ))}
        </div>
    );
}
