import { Suspense } from 'react';
import RelationshipList from "@/app/ui/relationship/relationship-list";

// app/sheep/relationship/page.tsx
export default function RelationshipPage() {
    return (
        <div>
            <h1 className="text-2xl font-bold">Relationship List</h1>

            <Suspense fallback={<p>Loading List...</p>}>
                <RelationshipList />
            </Suspense>
        </div>
    );
}