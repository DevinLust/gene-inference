import { fetchAllRelationships } from '@/app/lib/data';
import { Relationship } from '@/app/lib/definitions';
import { RelationshipDetails } from './buttons';

export default async function RelationshipList() {
    const relationships: Relationship[] = await fetchAllRelationships();
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
                                    {relationships.map((rel: Relationship) => (
                                        <tr
                                            key={rel.id}
                                            className="w-full border-b border-gray-600 py-3 text-sm last-of-type:border-none [&:first-child>td:first-child]:rounded-tl-lg [&:first-child>td:last-child]:rounded-tr-lg [&:last-child>td:first-child]:rounded-bl-lg [&:last-child>td:last-child]:rounded-br-lg"
                                        >
                                            <td className="whitespace-nowrap py-3 pl-6 pr-3">
                                                <span>{rel.id}</span>
                                            </td>
                                            <td className="whitespace-nowrap pr-3">
                                                <span>{rel.parent1.name}</span>
                                            </td>
                                            <td className="whitespace-nowrap pr-3">
                                                <span>{rel.parent2.name}</span>
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