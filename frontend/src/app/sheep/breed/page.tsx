import PredictionForm from '@/app/ui/breed/prediction-form';
import BestPredictions from '@/app/ui/breed/best-predictions';

export default function BreedingPage() {
    return (
        <div className="breeding-page">
            <h1 className="text-3xl mb-8">Breeding Page</h1>
            <div className="flex justify-start gap-6 items-start">
                <PredictionForm />
                <BestPredictions />
            </div>
        </div>
    );
}
