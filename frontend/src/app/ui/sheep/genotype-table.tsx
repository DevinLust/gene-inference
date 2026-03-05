import { Sheep, Category } from '@/app/lib/definitions';
import CategoryTag from '@/app/ui/category-tag';
import EvolveButton from '@/app/ui/sheep/evolve-button';

export default function GenotypeTable({ sheep }: { sheep: Sheep }) {
    return (
        <div className="mt-6 mb-4 w-full max-w-[50%] p-2 rounded-xl bg-gray-600">
            <h2 className="mb-2 text-xl">Genotypes</h2>
            <div className="p-2 bg-gray-800 rounded-lg border border-gray-500">
                <div className="bg-blue-900 rounded-md overflow-hidden">
                    <table className="w-full">
                        <thead>
                        <tr className="text-center bg-blue-500">
                            <th className="sticky left-0 bg-transparent px-2 py-2 font-semibold text-left">
                                Category
                            </th>
                            <th className="bg-transparent px-2 py-2 font-semibold">
                                Phenotype
                            </th>
                            <th className="bg-transparent px-2 py-2 font-semibold whitespace-nowrap">
                                Hidden Allele
                            </th>
                            <th scope="col" className="px-4 font-medium sm:pl-6">
                                <span className="sr-only">Evolve</span>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.entries(sheep.genotypes).map(([category, genotype]) => (
                            <tr key={category} className="border-t border-white/10 odd:bg-white/5 hover:bg-white/20 transition-colors">
                                <td className="p-2"><CategoryTag category={category as Category} /></td>
                                <td className="text-center p-2">{genotype.phenotype}</td>
                                <td className="text-center p-2">{genotype.hiddenAllele ?? "unknown"}</td>
                                <td className="text-center p-2"><EvolveButton sheepId={sheep.id} category={category as Category} /></td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}