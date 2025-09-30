import Link from 'next/link';

export function CreateSheep() {
    return (
        <Link
            href="/sheep/create"
            className="flex h-10 items-center rounded-lg bg-blue-600 px-4 text-sm font-medium text-white transition-colors hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
        >
            <span className="hidden md:block">Create Sheep +</span>
        </Link>
    );
}

export function SheepDetails({ sheepId }: { sheepId: string }) {
    return (
        <Link
            href={`/sheep/${sheepId}`}
            className="rounded text-blue-400 ml-2 hover:bg-gray-600 px-2 py-1"
        >
            <span>more details</span>
        </Link>
    );
}
