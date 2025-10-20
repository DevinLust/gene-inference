import PhenotypeDistributions from "./phentoype-distributions";
import CategoryTag from "@/app/ui/category-tag";
import { BestPrediction, Category, Grade } from "@/app/lib/definitions";

export default function BestPredictionBody({ bestPrediction }: { bestPrediction: BestPrediction }) {
    console.log(bestPrediction);
    return (
        <div className="w-full p-1">
            <div className="flex justify-between">
                <div>
                    <p>Parent 1</p>
                    <p>{bestPrediction.parent1.name || <span className="text-gray-400">(unnamed)</span>}</p>
                    <p>Id: {bestPrediction.parent1.id}</p>
                    <CategoryGrades bestCategoryGradeMap={bestPrediction.parent1BestCategoryGradeMap} />
                </div>
                <div>
                    <p>Parent 2</p>
                    <p>{bestPrediction.parent2.name || <span className="text-gray-400">(unnamed)</span>}</p>
                    <p>Id: {bestPrediction.parent2.id}</p>
                    <CategoryGrades bestCategoryGradeMap={bestPrediction.parent2BestCategoryGradeMap} />
                </div>
            </div>

            {bestPrediction.phenotypeDistributions && <PhenotypeDistributions phenotypeDistributions={bestPrediction.phenotypeDistributions} />}
        </div>
    );
};

function CategoryGrades({ bestCategoryGradeMap }: { bestCategoryGradeMap: Partial<Record<Category, Grade>> }) {
    return (
        <div>
            {Object.entries(bestCategoryGradeMap).map(([category, grade]) => (
                <div key={category} className="flex justify-start gap-2">
                    <CategoryTag category={category as Category} />
                    <p>- {grade}</p>
                </div>
            ))}
        </div>
    );
}
