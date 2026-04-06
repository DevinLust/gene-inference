import { LogEntry } from "@/app/lib/definitions";

export default function LBPEventLog({ log }: { log: LogEntry[] }) {
    return (<div className="rounded border border-gray-600 p-4">
        <h3 className="mb-3 font-semibold">Event Log</h3>
        <div className="space-y-2">
            {log.length === 0 ? (
                <div className="text-sm text-gray-400">No events yet.</div>
            ) : (
                log.map((entry, index) => (
                    <div
                        key={index}
                        className="rounded border border-gray-700 px-3 py-2 text-sm text-gray-200"
                    >
                        {entry.text}
                    </div>
                ))
            )}
        </div>
    </div>);
}
