"use client";

import { useEffect, useRef, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { createClient as createSupabaseClient } from "@/app/lib/supabase/browser";
import { Category } from "@/app/lib/definitions";

type RunEventType = "RUN_STARTED" | "STEP_EVENT" | "COMPLETED";
type RunStage = "MESSAGE_PASSING" | "BELIEF_UPDATE" | "COMPLETED";

type VisualNode = {
    id: string;
    type: "sheep" | "relationship";
    label: string;
    x: number;
    y: number;
    center: boolean;
};

type VisualEdge = {
    id: string;
    sourceId: string;
    targetId: string;
    type: "full" | "stub";
    visibleTarget: boolean;
    stubAngleRadians?: number | null;
    stubIndex?: number | null;
    stubCount?: number | null;
};

type VisualGraphSnapshot = {
    centerNodeId: string;
    nodes: VisualNode[];
    edges: VisualEdge[];
};

type RunStartedPayload = {
    totalSteps: number;
    currentStep: number;
    stage: "MESSAGE_PASSING" | "BELIEF_UPDATE" | "COMPLETED";
    graph: VisualGraphSnapshot;
};

type StepEventPayload = {
    stepIndex: number;
    totalSteps: number;
    stage: RunStage;
    message: string;
};

type CompletedPayload = {
    message: string;
};

type RunEvent = {
    type: RunEventType;
    runId: string;
    payload: RunStartedPayload | StepEventPayload | CompletedPayload;
};

type LogEntry = {
    kind: RunEventType;
    text: string;
};

const CATEGORIES: Category[] = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];

export default function SocketTest() {
    const stompRef = useRef<Client | null>(null);

    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [isConnected, setIsConnected] = useState(false);

    const [runId, setRunId] = useState<string | null>(null);
    const [currentStep, setCurrentStep] = useState(0);
    const [totalSteps, setTotalSteps] = useState(0);
    const [stage, setStage] = useState<RunStage | "IDLE">("IDLE");
    const [isCompleted, setIsCompleted] = useState(false);
    const [log, setLog] = useState<LogEntry[]>([]);
    const [graph, setGraph] = useState<VisualGraphSnapshot | null>(null);

    const [targetSheepId, setTargetSheepId] = useState<number | null>(null);
    const [selectedCategory, setSelectedCategory] = useState<Category>("SWIM");

    useEffect(() => {
        const supabase = createSupabaseClient();

        const loadInitialSession = async () => {
            const { data, error } = await supabase.auth.getSession();
            if (error) {
                console.error("Failed to get session:", error);
                return;
            }
            setAccessToken(data.session?.access_token ?? null);
        };

        loadInitialSession();

        const {
            data: { subscription },
        } = supabase.auth.onAuthStateChange((_event, session) => {
            setAccessToken(session?.access_token ?? null);
        });

        return () => {
            subscription.unsubscribe();
        };
    }, []);

    useEffect(() => {
        if (!accessToken) return;

        const client = new Client({
            brokerURL: "ws://localhost:8080/ws",
            reconnectDelay: 5000,
            connectHeaders: {
                Authorization: `Bearer ${accessToken}`,
            },
            debug: (msg) => console.log("STOMP:", msg),
        });

        client.onConnect = () => {
            setIsConnected(true);

            client.subscribe("/user/queue/run-events", (message: IMessage) => {
                const event: RunEvent = JSON.parse(message.body);
                handleRunEvent(event);
            });
        };

        client.onDisconnect = () => {
            setIsConnected(false);
        };

        client.onWebSocketClose = () => {
            setIsConnected(false);
        };

        client.onWebSocketError = (event) => {
            console.error("WebSocket error:", event);
        };

        client.onStompError = (frame) => {
            console.error("STOMP broker error:", frame.headers["message"], frame.body);
        };

        client.activate();
        stompRef.current = client;

        return () => {
            setIsConnected(false);
            client.deactivate();
        };
    }, [accessToken]);

    function handleRunEvent(event: RunEvent) {
        switch (event.type) {
            case "RUN_STARTED": {
                const payload = event.payload as RunStartedPayload;

                setRunId(event.runId);
                setCurrentStep(payload.currentStep);
                setTotalSteps(payload.totalSteps);
                setStage(payload.stage);
                setIsCompleted(false);
                setGraph(payload.graph);

                setLog([
                    {
                        kind: "RUN_STARTED",
                        text: `Run started (${event.runId})`,
                    },
                ]);
                break;
            }

            case "STEP_EVENT": {
                const payload = event.payload as StepEventPayload;
                setCurrentStep(payload.stepIndex);
                setTotalSteps(payload.totalSteps);
                setStage(payload.stage);
                setLog((prev) => [
                    ...prev,
                    {
                        kind: "STEP_EVENT",
                        text: `Step ${payload.stepIndex}/${payload.totalSteps} [${payload.stage}] - ${payload.message}`,
                    },
                ]);
                break;
            }

            case "COMPLETED": {
                const payload = event.payload as CompletedPayload;
                setStage("COMPLETED");
                setIsCompleted(true);
                setLog((prev) => [
                    ...prev,
                    {
                        kind: "COMPLETED",
                        text: `Completed - ${payload.message}`,
                    },
                ]);
                break;
            }

            default:
                console.warn("Unknown event type:", event);
        }
    }

    function startRun() {
        setGraph(null);
        const client = stompRef.current;
        if (!client || !client.connected || targetSheepId == null) return;

        sessionStorage.removeItem("activeRunId");

        client.publish({
            destination: "/app/run.start",
            body: JSON.stringify({
                sheepId: targetSheepId,
            }),
        });
    }

    function nextStep() {
        const client = stompRef.current;
        if (!client || !client.connected || !runId || isCompleted) return;

        client.publish({
            destination: "/app/run.nextStep",
            body: JSON.stringify({
                runId,
                category: selectedCategory,
            }),
        });
    }

    function rayExitOffset(
        ux: number,
        uy: number,
        halfWidth: number,
        halfHeight: number,
        padding = 6
    ) {
        const tx = Math.abs(ux) > 1e-6 ? halfWidth / Math.abs(ux) : Number.POSITIVE_INFINITY;
        const ty = Math.abs(uy) > 1e-6 ? halfHeight / Math.abs(uy) : Number.POSITIVE_INFINITY;

        const t = Math.min(tx, ty) + padding;

        return {
            dx: ux * t,
            dy: uy * t,
        };
    }

    function rectExitOffset(
        ux: number,
        uy: number,
        halfWidth: number,
        halfHeight: number,
        padding = 0
    ) {
        const tx = Math.abs(ux) > 1e-6 ? halfWidth / Math.abs(ux) : Number.POSITIVE_INFINITY;
        const ty = Math.abs(uy) > 1e-6 ? halfHeight / Math.abs(uy) : Number.POSITIVE_INFINITY;
        const t = Math.min(tx, ty) + padding;
        return { dx: ux * t, dy: uy * t };
    }

    const GRAPH_WIDTH = 700;
    const GRAPH_HEIGHT = 500;
    const CENTER_X = GRAPH_WIDTH / 2;
    const CENTER_Y = GRAPH_HEIGHT / 2;

    const graphScale = (() => {
        if (!graph || graph.nodes.length === 0) return 1;

        const maxX = Math.max(...graph.nodes.map((n) => Math.abs(n.x)));
        const maxY = Math.max(...graph.nodes.map((n) => Math.abs(n.y)));

        const paddingX = 140;
        const paddingY = 100;

        const usableHalfWidth = GRAPH_WIDTH / 2 - paddingX;
        const usableHalfHeight = GRAPH_HEIGHT / 2 - paddingY;

        const scaleX = maxX > 0 ? usableHalfWidth / maxX : 1;
        const scaleY = maxY > 0 ? usableHalfHeight / maxY : 1;

        return Math.min(1, scaleX, scaleY);
    })();

    return (
        <div className="p-6 space-y-4 text-white">
            <div className="space-y-1">
                <div>Socket: {isConnected ? "Connected" : "Disconnected"}</div>
                <div>Run ID: {runId ?? "None"}</div>
                <div>Stage: {stage}</div>
                <div>
                    Step: {currentStep} / {totalSteps}
                </div>
                <div>Status: {isCompleted ? "Completed" : "Active / Idle"}</div>
            </div>

            <div className="flex gap-3">
                <input
                    type="number"
                    value={targetSheepId ?? ""}
                    onChange={(e) => setTargetSheepId(e.target.value ? Number(e.target.value) : null)}
                    className="rounded border border-gray-500 px-3 py-2 text-white"
                    placeholder="Target sheep id"
                />
                <select
                    value={selectedCategory}
                    onChange={(e) => setSelectedCategory(e.target.value as Category)}
                    className="rounded border border-gray-500 px-3 py-2 text-white"
                >
                    {CATEGORIES.map((c) => (
                        <option key={c} value={c}>
                            {c}
                        </option>
                    ))}
                </select>
                <button
                    onClick={startRun}
                    disabled={!isConnected || targetSheepId == null}
                    className="rounded bg-blue-600 px-4 py-2 disabled:opacity-50"
                >
                    Start Run
                </button>

                <button
                    onClick={nextStep}
                    disabled={!isConnected || !runId || isCompleted}
                    className="rounded bg-green-600 px-4 py-2 disabled:opacity-50"
                >
                    Next Step
                </button>
            </div>

            <div className="rounded border border-gray-600 p-4">
                <h3 className="mb-3 font-semibold">Event Log</h3>
                <div className="space-y-2">
                    {graph && (
                        <div className="rounded border border-gray-600 p-4">
                            <h3 className="mb-3 font-semibold">Graph Preview</h3>

                            {/* 👇 THIS is the new wrapper */}
                            <div className="overflow-auto max-h-[500px] max-w-full">
                                <svg
                                    width={1200}
                                    height={800}
                                    viewBox="0 0 1200 800"
                                    className="rounded border border-gray-700 bg-gray-900"
                                >
                                {(() => {
                                    const SVG_WIDTH = 1200;
                                    const SVG_HEIGHT = 800;

                                    const CENTER_X = SVG_WIDTH / 2;
                                    const CENTER_Y = SVG_HEIGHT / 2;

                                    const nodeSize = (type: "sheep" | "relationship") =>
                                        type === "relationship"
                                            ? { width: 200, height: 80 }
                                            : { width: 160, height: 80 };

                                    const nodeMap = new Map(
                                        graph.nodes.map((n) => [
                                            n.id,
                                            {
                                                ...n,
                                                cx: CENTER_X + n.x,
                                                cy: CENTER_Y + n.y,
                                                ...nodeSize(n.type),
                                            },
                                        ])
                                    );

                                    return (
                                        <>
                                            {/* Edges */}
                                            {graph.edges.map((edge) => {
                                                const source = nodeMap.get(edge.sourceId);
                                                const target = nodeMap.get(edge.targetId);

                                                if (!source) return null;

                                                if (edge.type === "full") {
                                                    if (!target) return null;

                                                    const dx = target.cx - source.cx;
                                                    const dy = target.cy - source.cy;
                                                    const len = Math.sqrt(dx * dx + dy * dy) || 1;
                                                    const ux = dx / len;
                                                    const uy = dy / len;

                                                    const sourceExit = rectExitOffset(
                                                        ux,
                                                        uy,
                                                        source.width / 2,
                                                        source.height / 2,
                                                        2
                                                    );

                                                    const targetExit = rectExitOffset(
                                                        -ux,
                                                        -uy,
                                                        target.width / 2,
                                                        target.height / 2,
                                                        2
                                                    );

                                                    const x1 = source.cx + sourceExit.dx;
                                                    const y1 = source.cy + sourceExit.dy;
                                                    const x2 = target.cx + targetExit.dx;
                                                    const y2 = target.cy + targetExit.dy;

                                                    return (
                                                        <line
                                                            key={edge.id}
                                                            x1={x1}
                                                            y1={y1}
                                                            x2={x2}
                                                            y2={y2}
                                                            stroke="white"
                                                            strokeWidth="3"
                                                            strokeLinecap="round"
                                                        />
                                                    );
                                                }

                                                const angle = edge.stubAngleRadians ?? 0;
                                                const ux = Math.cos(angle);
                                                const uy = Math.sin(angle);

                                                const sourceExit = rectExitOffset(
                                                    ux,
                                                    uy,
                                                    source.width / 2,
                                                    source.height / 2,
                                                    4
                                                );

                                                const x1 = source.cx + sourceExit.dx;
                                                const y1 = source.cy + sourceExit.dy;

                                                const stubLength = 65;
                                                const x2 = x1 + ux * stubLength;
                                                const y2 = y1 + uy * stubLength;

                                                return (
                                                    <g key={edge.id}>
                                                        <line
                                                            x1={x1}
                                                            y1={y1}
                                                            x2={x2}
                                                            y2={y2}
                                                            stroke="orange"
                                                            strokeWidth="4"
                                                            strokeDasharray="8 6"
                                                            strokeLinecap="round"
                                                        />
                                                        <circle cx={x2} cy={y2} r="6" fill="orange" />
                                                    </g>
                                                );
                                            })}

                                            {/* Nodes */}
                                            {Array.from(nodeMap.values()).map((node) => {
                                                const x = node.cx - node.width / 2;
                                                const y = node.cy - node.height / 2;
                                                const fill =
                                                    node.type === "relationship" ? "#a000e8" : "#2563eb";
                                                const stroke = node.center ? "#93c5fd" : "#d8b4fe";

                                                return (
                                                    <g key={node.id}>
                                                        <rect
                                                            x={x}
                                                            y={y}
                                                            width={node.width}
                                                            height={node.height}
                                                            rx={10}
                                                            fill={fill}
                                                            stroke={stroke}
                                                            strokeWidth={2}
                                                        />
                                                        <text
                                                            x={node.cx}
                                                            y={node.cy - 4}
                                                            fill="white"
                                                            fontSize="18"
                                                            textAnchor="middle"
                                                        >
                                                            {node.label}
                                                        </text>
                                                        <text
                                                            x={node.cx}
                                                            y={node.cy + 20}
                                                            fill="rgba(255,255,255,0.7)"
                                                            fontSize="12"
                                                            textAnchor="middle"
                                                        >
                                                            {node.id}
                                                        </text>
                                                    </g>
                                                );
                                            })}
                                        </>
                                    );
                                })()}
                            </svg>
                            </div>
                        </div>
                    )}
                    {log.length === 0 ? (
                        <div className="text-gray-400">No events yet.</div>
                    ) : (
                        log.map((entry, index) => (
                            <div
                                key={index}
                                className="rounded bg-gray-800 px-3 py-2 text-sm"
                            >
                                {entry.text}
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}
