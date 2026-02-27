import { BirthRecordRow, BirthRecordFilter } from '@/app/lib/definitions';
import { fetchBirthRecordRows } from '@/app/lib/data';
import Link from 'next/link';


export default async function BirthRecordList({ filter }: { filter: BirthRecordFilter }) {
    const birthRecordRows: BirthRecordRow[] = await fetchBirthRecordRows(filter);
    return (
        <div className="mt-6 flow-root">
            <div className="inline-block align-middle">
                <div className="rounded-lg bg-gray-600 p-2 md:pt-0">
                    <table>
                        <thead>
                        <tr>
                            <th scope="col" className="pt-3 pl-6 pr-3 font-medium text-left">
                                ID
                            </th>
                            <th scope="col" className="px-4 pt-5 font-medium text-center" colSpan={1}>
                                P1
                            </th>
                            <th scope="col" className="px-4 pt-5 font-medium text-center" colSpan={1}>
                                P2
                            </th>
                            <th scope="col" className="px-4 pt-5 font-medium text-center">
                                Child
                            </th>
                            <th scope="col" className="px-4 py-5 font-medium sm:pl-6">
                                <span className="sr-only">Details</span>
                            </th>
                        </tr>
                        </thead>
                        <tbody  className="bg-gray-800">
                        {birthRecordRows.map((br: BirthRecordRow) => (
                            <tr
                                key={br.id}
                                className="w-full border-b border-gray-600 py-3 text-sm last-of-type:border-none [&:first-child>td:first-child]:rounded-tl-lg [&:first-child>td:last-child]:rounded-tr-lg [&:last-child>td:first-child]:rounded-bl-lg [&:last-child>td:last-child]:rounded-br-lg"
                            >
                                <td className="whitespace-nowrap py-3 pl-6 pr-3 text-right tabular-nums">
                                    {br.id}
                                </td>
                                <td className="whitespace-nowrap py-3 px-6">
                                    <Link
                                        href={`/sheep/${br.parent1Id}`}
                                        className="flex items-baseline gap-2 hover:underline"
                                    >
                                        <span className="w-12 text-right tabular-nums">
                                          {br.parent1Id}:
                                        </span>
                                        <span className="text-left">
                                          {br.parent1Name || (
                                              <span className="text-gray-400">(unnamed)</span>
                                          )}
                                        </span>
                                    </Link>
                                </td>
                                <td className="whitespace-nowrap py-3 px-6">
                                    <Link
                                        href={`/sheep/${br.parent2Id}`}
                                        className="flex items-baseline gap-2 hover:underline"
                                    >
                                        <span className="w-12 text-right tabular-nums">
                                          {br.parent2Id}:
                                        </span>
                                        <span className="text-left">
                                          {br.parent2Name || (
                                              <span className="text-gray-400">(unnamed)</span>
                                          )}
                                        </span>
                                    </Link>
                                </td>
                                <td className="whitespace-nowrap py-3 pl-6 pr-3 text-center tabular-nums">
                                    {br.childId ?
                                        <Link
                                            href={`/sheep/${br.parent1Id}`}
                                            className="flex items-baseline gap-2 hover:underline"
                                        >
                                            <span className="w-12 text-right tabular-nums">
                                              {br.childId}:
                                            </span>
                                                <span className="text-left">
                                              {br.childName || (
                                                  <span className="text-gray-400">(unnamed)</span>
                                              )}
                                            </span>
                                        </Link>
                                        :
                                        <div className="flex items-baseline gap-2">
                                            <span className="w-12 text-right text-gray-500">X:</span>
                                            <span className="text-gray-500">not saved</span>
                                        </div>
                                    }
                                </td>
                                <td className="whitespace-nowrap py-3 pl-6 pr-3">
                                    <Link
                                        href={`/birth-record/${br.id}`}
                                        className="p-1 hover:bg-gray-600 rounded text-blue-400"
                                    >
                                        more details
                                    </Link>
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