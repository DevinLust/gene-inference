import { Sheep, Category } from '@/app/lib/definitions';
import CategoryTag from '@/app/ui/category-tag';

export default function GenotypeTable({ sheep }: { sheep: Sheep }) {
    return (
        <div className="mt-6 mb-4 w-full max-w-sm p-2 rounded-xl bg-gray-600">
            <h2 className="mb-2 text-xl">Genotypes</h2>
            <div className="p-2 bg-gray-800 rounded-lg border border-gray-500">
            <table className="max-w-md w-full bg-blue-900">
                <thead>
                <tr className="text-center bg-blue-500">
                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-left">
                        Category
                    </th>
                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold">
                        Phenotype
                    </th>
                    <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold">
                        Hidden Allele
                    </th>
                </tr>
                </thead>
                <tbody>
                {Object.entries(sheep.genotypes).map(([category, genotype]) => (
                    <tr key={category} className="border-t border-white/10 odd:bg-white/5 hover:bg-white/20 transition-colors">
                        <td className="p-2"><CategoryTag category={category as Category} /></td>
                        <td className="text-center p-2">{genotype.phenotype}</td>
                        <td className="text-center p-2">{genotype.hiddenAllele ?? "unknown"}</td>
                    </tr>
                ))}
                </tbody>
            </table>
            </div>
        </div>
    );
}