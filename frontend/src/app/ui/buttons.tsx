import Link from 'next/link';
import { ReactNode } from "react";

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

type SheepDetailsProps = {
  sheepId: string | number;
  children?: ReactNode;
  className?: string;
};

export function SheepDetails({ sheepId, children, className }: SheepDetailsProps) {
  return (
    <Link
      href={`/sheep/${sheepId}`}
      className={`rounded text-blue-400 ml-2 hover:bg-gray-600 px-2 py-1 ${className ?? ""}`}
    >
      {children ?? <span>more details</span>}
    </Link>
  );
}

export function RelationshipDetails({ relId }: { relId: string | number }) {
  return (
      <Link
          href={`/relationship/${relId}`}
          className="rounded text-blue-400 ml-2 hover:bg-gray-600 px-2 py-1"
      >
          <span>more details</span>
      </Link>
  );
}

export function BreedSheepButton() {
    return (
        <Link
            href={"/breed/create"}
            className="flex h-10 items-center rounded-lg bg-blue-600 px-4 text-sm font-medium text-white transition-colors hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
        >
            <span className="hidden md:block">Breed Sheep +</span>
        </Link>
    );
}

export function SheepEditButton({ sheepId, children, className }: SheepDetailsProps) {
    return (
        <Link
            href={`/sheep/${sheepId}/edit`}
            className={`rounded text-blue-400 ml-2 hover:bg-gray-600 px-2 py-1 ${className ?? ""}`}
        >
            {children ?? <span>edit</span>}
        </Link>
    );
}

export function EpochRecordButton({ relationshipId, category, p1, p2 }: { relationshipId: string | number, category: string, p1: string, p2: string}) {
    return (
        <Link
            href={`/birth-record?relationshipId=${relationshipId}&category=${category}&p1=${p1}&p2=${p2}`}
            className={`rounded text-gray-200 ml-2 hover:bg-gray-600 px-2 py-1`}
        >
            view records
        </Link>
    );
}
