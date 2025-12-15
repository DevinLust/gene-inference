import Link from 'next/link';

export function RelationshipDetails({ relId }: { relId: string | number }) {
    return (
        <Link
            href={`/sheep/relationship/${relId}`}
            className="rounded text-blue-400 ml-2 hover:bg-gray-600 px-2 py-1"
        >
            <span>more details</span>
        </Link>
    );
}
