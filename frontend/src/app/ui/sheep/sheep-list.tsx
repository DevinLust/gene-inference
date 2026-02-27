'use server';

import { SheepSummary, Category, Grade, Distributions, DistributionType, SheepFilter } from '@/app/lib/definitions';
import { fetchAllSheep, fetchDistributions } from '@/app/lib/data';
import { SheepDetails } from '@/app/ui/buttons';
import CategoryDropdown from './category-dropdown';

const ALL_GRADES: Grade[] = ["S", "A", "B", "C", "D", "E"];

export default async function SheepList({ selectedCategory, filter }: { selectedCategory: Category, filter: SheepFilter }) {
    const sheep: SheepSummary[] = await fetchAllSheep(filter);
    const displayCat: Category = selectedCategory ?? "SWIM";
    const distributions: Distributions = await fetchDistributions({ category: displayCat, type: ("INFERRED" as DistributionType) });
    return (
        <div className="mt-6 flow-root">
            <CategoryDropdown />

            <div className="inline-block align-middle">
                <div className="rounded-lg bg-gray-600 p-2 md:pt-0">
                    <table>
                        <thead>
                            <tr>
                                <th scope="col" className="pt-3 pl-6 pr-3 font-medium text-left">

                                </th>
                                <th scope="col" className="px-4 pt-5 font-medium text-center" colSpan={ALL_GRADES.length}>
                                    Probability
                                </th>
                                <th scope="col" className="px-4 pt-5 font-medium sm:pl-6">

                                </th>
                            </tr>
                            <tr>
                                <th scope="col" className="py-3 pl-6 pr-3 font-medium text-left">
                                    Name
                                </th>
                                {ALL_GRADES.map(grade => (
                                    <th key={grade} className="px-4 py-5 font-medium text-center" scope="col">
                                        {grade}
                                    </th>
                                ))}
                                <th scope="col" className="px-4 py-5 font-medium sm:pl-6">
                                    <span className="sr-only">Details</span>
                                </th>
                            </tr>
                        </thead>
                        <tbody  className="bg-gray-800">
                            {sheep.map((s: SheepSummary) => (
                                <tr
                                    key={s.id}
                                    className="w-full border-b border-gray-600 py-3 text-sm last-of-type:border-none [&:first-child>td:first-child]:rounded-tl-lg [&:first-child>td:last-child]:rounded-tr-lg [&:last-child>td:first-child]:rounded-bl-lg [&:last-child>td:last-child]:rounded-br-lg"
                                >
                                    <td className="whitespace-nowrap py-3 pl-6 pr-3">
                                        <span>{s.name || <span className="text-gray-400">(unnamed)</span>}</span>
                                    </td>
                                    {ALL_GRADES.map(grade => {
                                        const prob = distributions.distributions[s.id][grade];
                                        return (
                                            <td
                                                className={`whitespace-nowrap py-3 pr-3 text-right ${
                                                    prob >= 0.2 ? "text-white" : "text-white/80"
                                                }`}
                                                key={grade}
                                            >
                                                {(100 * prob).toFixed(2)}%
                                            </td>
                                        );
                                    })}
                                    <td className="whitespace-nowrap py-3 pr-3">
                                        <SheepDetails sheepId={s.id} />
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
