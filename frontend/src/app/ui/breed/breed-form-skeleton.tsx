export default function BreedFormSkeleton() {
    return (
        <div className="flex">
            <div className="flex flex-col gap-6 p-4 max-w-md w-full bg-gray-600 rounded-lg animate-pulse">

                {/* Title */}
                <div className="h-6 w-40 bg-gray-500 rounded" />

                {/* Parent 1 */}
                <div className="flex flex-col gap-2">
                    <div className="h-4 w-24 bg-gray-500 rounded" />
                    <div className="h-10 w-full bg-gray-500 rounded" />
                    <div className="h-4 w-48 bg-gray-500 rounded mt-2" />
                </div>

                {/* Parent 2 */}
                <div className="flex flex-col gap-2">
                    <div className="h-4 w-24 bg-gray-500 rounded" />
                    <div className="h-10 w-full bg-gray-500 rounded" />
                    <div className="h-4 w-48 bg-gray-500 rounded mt-2" />
                </div>

                {/* Save child checkbox */}
                <div className="flex items-center gap-3">
                    <div className="h-4 w-4 bg-gray-500 rounded" />
                    <div className="h-4 w-32 bg-gray-500 rounded" />
                </div>

                {/* Offspring name input */}
                <div className="h-10 w-full bg-gray-500 rounded" />

                {/* Submit button */}
                <div className="h-10 w-full bg-blue-500/40 rounded" />

            </div>
        </div>
    );
}
