import { BirthRecord, Category } from '@/app/lib/definitions';
import CategoryTag from '@/app/ui/category-tag';

export default function PhenotypesAtBirthTable({ birthRecord }: {birthRecord: BirthRecord }) {
    return (
        <div className="inline-block min-w-1/2 bg-gray-600 rounded-lg mt-4">
            <h1 className="border-b border-gray-400 pl-6 py-1">Phenotypes at birth</h1>
            <div className="p-2">
                <table className="bg-blue-800 min-w-full border-separate border-spacing-0 text-sm">
                    <thead>
                    <tr className="bg-blue-500">
                        <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-left">
                            Category
                        </th>
                        <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-center">
                            Parent 1
                        </th>
                        <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-center">
                            Parent 2
                        </th>
                        <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-center">
                            Child
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.entries(birthRecord.phenotypesAtBirth).map(([category, phenotypes]) => (
                        <tr key={category} className="border-t border-white/10 odd:bg-white/5 hover:bg-white/20 transition-colors">
                            <td className="sticky left-0 bg-transparent px-2 py-2 font-mono">
                                <CategoryTag category={category as Category} />
                            </td>
                            <td className="sticky left-0 bg-transparent px-2 py-2 font-mono text-center">
                                {phenotypes.parent1}
                            </td>
                            <td className="sticky left-0 bg-transparent px-2 py-2 font-mono text-center">
                                {phenotypes.parent2}
                            </td>
                            <td className="sticky left-0 bg-transparent px-2 py-2 font-mono text-center">
                                {phenotypes.child}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}