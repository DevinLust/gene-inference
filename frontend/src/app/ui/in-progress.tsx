
type FeatureProps = {
    children: React.ReactNode;
}

export default function FeatureInProgress({ children }: FeatureProps) {
    return (
        <div className="relative opacity-70 pointer-events-none border border-white">
            {children}

            {/* Optional overlay: remove if you don’t want text */}
            <div className="absolute inset-0 bg-gray-900/20 backdrop-blur-lg flex items-center justify-center pointer-events-none">
                <span className="text-gray-300 text-sm bg-gray-800/70 px-3 py-1 rounded-md">
                  Feature in progress
                </span>
            </div>
        </div>
    );
}
