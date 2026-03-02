import ServerPredictionForm from '@/app/ui/breed/server-prediction-form';
import PredictionFormSkeleton from '@/app/ui/breed/prediction-form-skeleton';
import BestPredictions from '@/app/ui/breed/best-predictions';
import { BreedSheepButton, RecordChildButton } from '@/app/ui/buttons';
import { Suspense } from "react";

export default function BreedingPage() {
    return (
        <div className="breeding-page">
            <h1 className="text-3xl mb-8">Breeding Page</h1>
            <div className="mt-4 mb-4 flex items-center justify-start gap-2 md:mt-8">
                <BreedSheepButton />
                <RecordChildButton />
            </div>
            <div className="flex justify-start gap-6 items-start">
                <Suspense fallback={<PredictionFormSkeleton />}>
                    <ServerPredictionForm />
                </Suspense>
                <BestPredictions />
            </div>
        </div>
    );
}
