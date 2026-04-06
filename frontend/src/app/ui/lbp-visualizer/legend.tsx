import { RunStage } from "@/app/lib/definitions";

export default function Legend({
                    stage,
                }: {
    stage: RunStage | "IDLE";
}) {
    return (
        <div className="rounded border border-gray-600 p-3 text-sm space-y-2 max-w-sm">
            <div className="font-semibold">Legend</div>

            {stage === "MESSAGE_PASSING" && (
                <>
                    <div>
                        <div className="text-gray-300 mb-1">Sheep → Relationship</div>
                        <LegendRow color="#22d3ee" label="Parent edge" />
                        <LegendRow color="#a78bfa" label="Child edge" />
                    </div>

                    <div>
                        <div className="text-gray-300 mb-1">Relationship → Sheep</div>
                        <LegendRow color="#facc15" label="Parent edge" />
                        <LegendRow color="#fb7185" label="Child edge" />
                    </div>
                </>
            )}

            {stage === "BELIEF_UPDATE" && (
                <div>
                    <div className="text-gray-300 mb-1">Belief Update</div>
                    <LegendRow color="#84cc16" label="Parent edge" />
                    <LegendRow color="#14b8a6" label="Child edge" />
                </div>
            )}
        </div>
    );
}

function LegendRow({
                       color,
                       label,
                   }: {
    color: string;
    label: string;
}) {
    return (
        <div className="flex items-center gap-2">
            <div
                className="w-4 h-4 rounded"
                style={{ backgroundColor: color }}
            />
            <span>{label}</span>
        </div>
    );
}
