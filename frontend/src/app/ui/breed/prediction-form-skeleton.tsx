export default function PredictionFormSkeleton() {
    return (
        <div className="flex flex-col gap-4 p-4 max-w-md bg-gray-600 rounded-lg animate-pulse">
            <div className="h-6 bg-gray-500 rounded w-1/2" />
            <div className="h-10 bg-gray-500 rounded" />
            <div className="h-10 bg-gray-500 rounded" />
            <div className="h-10 bg-gray-500 rounded w-32" />
        </div>
    );
}
