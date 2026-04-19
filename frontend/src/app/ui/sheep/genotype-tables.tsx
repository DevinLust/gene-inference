import { Sheep, APPEARANCE_CATEGORIES, TRAIT_CATEGORIES } from "@/app/lib/definitions";
import GenotypeTable from "./genotype-table";

export default function GenotypeTables({ sheep }: { sheep: Sheep }) {
    return (
        <div className="my-6 grid grid-cols-1 gap-4 xl:grid-cols-2 items-start">
            <GenotypeTable
                sheep={sheep}
                title="Appearance Genes"
                categories={APPEARANCE_CATEGORIES}
                showEvolve={false}
            />
            <GenotypeTable
                sheep={sheep}
                title="Stat Genes"
                categories={TRAIT_CATEGORIES}
                showEvolve={true}
            />
        </div>
    );
}