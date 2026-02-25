import { Sheep } from '@/app/lib/definitions';
import { fetchAllSheep } from '@/app/lib/data';
import PredictionForm from '@/app/ui/breed/prediction-form';
import BestPredictions from '@/app/ui/breed/best-predictions';
import { BreedSheepButton } from '@/app/ui/buttons';

export default async function BreedingPage() {
    const sheep: Sheep[] = await fetchAllSheep()
    return (
        <div className="breeding-page">
            <h1 className="text-3xl mb-8">Breeding Page</h1>
            <div className="mt-4 mb-4 flex items-center justify-between gap-2 md:mt-8">
                <BreedSheepButton />
            </div>
            <div className="flex justify-start gap-6 items-start">
                <PredictionForm sheep={sheep} />
                <BestPredictions />
            </div>
        </div>
    );
}
