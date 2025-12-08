import { PhenotypeDistributions, Grade, Category } from '@/app/lib/definitions';
import CategoryCard from "@/app/ui/category-card";

export default function PhenotypeDistributionsList({ phenotypeDistributions }: { phenotypeDistributions: PhenotypeDistributions }) {
    return (
        <div className="grid grid-cols-1 gap-4 p-2 bg-gray-800 rounded-lg">
            {Object.entries(phenotypeDistributions).map(([cat, distribution]) => (
                <CategoryCard key={`${cat}`} category={cat as Category}>
                    <div key={`${cat}`} className="grid grid-cols-6 gap-2 pt-1">
                        {Object.entries(distribution).map(([grade, prob]) => (
                            <p key={`${cat}.${grade}`}>{grade}: {(prob * 100).toFixed(2)}%</p>
                        ))}
                    </div>
                </CategoryCard>
            ))}
        </div>
    );
}
