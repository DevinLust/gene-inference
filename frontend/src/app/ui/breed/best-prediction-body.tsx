import PhenotypeDistributions from "./phentoype-distributions";
import CategoryTag from "@/app/ui/category-tag";
import { BestPrediction, Category, Grade } from "@/app/lib/definitions";

export default function BestPredictionBody({ bestPrediction }: { bestPrediction: BestPrediction }) {
    return (
        <div className="w-full p-1">
            <div className="flex justify-around items-center my-1">
                <div className="w-1/4 self-start grid grid-cols-1 gap-1 content-start bg-blue-800 rounded-lg">
                    <p className="mx-1">{bestPrediction.parent1.name || <span className="text-gray-400">(unnamed)</span>}</p>
                    <p className="mx-1">Id: {bestPrediction.parent1.id}</p>
                    <CategoryGrades bestCategoryGradeMap={bestPrediction.parent1BestCategoryGradeMap} />
                </div>
                <div className="w-1/4 self-start grid grid-cols-1 gap-1 content-start bg-blue-800 rounded-lg">
                    <p className="mx-1">{bestPrediction.parent2.name || <span className="text-gray-400">(unnamed)</span>}</p>
                    <p className="mx-1">Id: {bestPrediction.parent2.id}</p>
                    <CategoryGrades bestCategoryGradeMap={bestPrediction.parent2BestCategoryGradeMap} />
                </div>
            </div>

            {bestPrediction.phenotypeDistributions && <PhenotypeDistributions phenotypeDistributions={bestPrediction.phenotypeDistributions} />}
        </div>
    );
};

function CategoryGrades({ bestCategoryGradeMap }: { bestCategoryGradeMap: Partial<Record<Category, Grade>> }) {
    return (
        <div className="grid grid-cols-1 content-start">
            {Object.entries(bestCategoryGradeMap).map(([category, grade], index, arr) => (
                <p
                    key={category}
                    className={`
                      p-1 
                      odd:bg-blue-500 
                      even:bg-blue-600
                      ${index === arr.length - 1 ? "rounded-b-lg" : ""}
                    `}
                >
                    <CategoryTag category={category as Category} /> - {grade}
                </p>
            ))}
        </div>
    );
}
