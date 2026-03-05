import { fetchAllRelationships } from '@/app/lib/data';
import { RelationshipRow } from '@/app/lib/definitions';
import { RelationshipDetails } from '@/app/ui/buttons';
import EmptyState from '@/app/ui/empty-state';

export default async function RelationshipList() {
    const relationships: RelationshipRow[] = await fetchAllRelationships();

    if (relationships.length === 0) {
        return (
            <EmptyState
                title="No relationships yet"
                description="This is where you'll see parent sheep that have produced offspring together."
            />
        );
    }

    return (
        <div className="mt-6 flow-root">
            <div className="inline-block align-middle">
                <div className="rounded-lg bg-gray-600 p-2 md:pt-0">
                    <table>
                        <thead>
                            <tr>
                                <th scope="col" className="py-3 pl-6 pr-3 font-medium text-left">
                                    ID
                                </th>
                                <th scope="col" className="front-medium pr-3">
                                    Parent1
                                </th>
                                <th scope="col" className="front-medium pr-3">
                                    Parent2
                                </th>
                                <th scope="col" className="px-4 py-5 font-medium sm:pl-6">
                                    <span className="sr-only">Details</span>
                                </th>
                            </tr>
                        </thead>
                        <tbody  className="bg-gray-800">
                            {relationships.map((rel: RelationshipRow) => (
                                <tr
                                    key={rel.id}
                                    className="w-full border-b border-gray-600 py-3 text-sm last-of-type:border-none [&:first-child>td:first-child]:rounded-tl-lg [&:first-child>td:last-child]:rounded-tr-lg [&:last-child>td:first-child]:rounded-bl-lg [&:last-child>td:last-child]:rounded-br-lg"
                                >
                                    <td className="whitespace-nowrap py-3 pl-6 pr-3">
                                        <span>{rel.id}</span>
                                    </td>
                                    <td className="whitespace-nowrap pr-3 text-center">
                                        <span>{rel.parent1Name || <span className="text-gray-400">(id: {rel.parent1Id})</span>}</span>
                                    </td>
                                    <td className="whitespace-nowrap pr-3 text-center">
                                        <span>{rel.parent2Name || <span className="text-gray-400">(id: {rel.parent2Id})</span>}</span>
                                    </td>
                                    <td className="whitespace-nowrap py-3 pr-3">
                                        <RelationshipDetails relId={rel.id}/>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}