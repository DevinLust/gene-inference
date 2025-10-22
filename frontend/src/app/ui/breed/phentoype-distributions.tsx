import { PhenotypeDistributions, Grade, Category } from '@/app/lib/definitions';
import CategoryTag from "@/app/ui/category-tag";

export default function PhenotypeDistributionsList({ phenotypeDistributions }: { phenotypeDistributions: PhenotypeDistributions }) {
    return (
        <div className="grid grid-cols-1 gap-4 p-2 bg-gray-800 rounded-lg">
            {Object.entries(phenotypeDistributions).map(([cat, distribution]) => (
                <div key={`${cat}`} className="border border-gray-500 rounded-lg bg-blue-900">
                    <div className="w-full bg-blue-500 pl-4 py-1 rounded-t-lg">
                        <CategoryTag category={cat as Category} />
                    </div>
                    <div key={`${cat}`} className="grid grid-cols-6 gap-2 px-2 pt-1">
                        {Object.entries(distribution).map(([grade, prob]) => (
                            <p key={`${cat}.${grade}`}>{grade}: {(prob * 100).toFixed(2)}%</p>
                        ))}
                    </div>
                </div>
            ))}
        </div>
    );
}
