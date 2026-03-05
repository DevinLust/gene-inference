import Link from "next/link";

type EmptyStateProps = {
    title: string;
    description: string;
    actionLabel?: string;
    actionHref?: string;
};

export default function EmptyState({
                                       title,
                                       description,
                                       actionLabel,
                                       actionHref,
                                   }: EmptyStateProps) {
    return (
        <div className="flex flex-col items-center justify-center rounded-xl border border-gray-800 bg-gray-900/40 p-10 text-center">
            <h2 className="text-lg font-semibold text-white">{title}</h2>
            <p className="mt-2 max-w-md text-sm text-gray-400">{description}</p>

            {actionHref && actionLabel && (
                <Link
                    href={actionHref}
                    className="mt-4 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-500"
                >
                    {actionLabel}
                </Link>
            )}
        </div>
    );
}
