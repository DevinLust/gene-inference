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
    centerSheepId: string;
    nodes: VisualNode[];
    edges: VisualEdge[];
};

type MessageWaveDelta = {
    waveType: "SHEEP_TO_RELATIONSHIP" | "RELATIONSHIP_TO_SHEEP";
    category: string;
    activeFullEdgeIds: string[];
    activeStubEdgeIds: string[];
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
    delta?: MessageWaveDelta | null;
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

export default function LoopyBeliefVisualizer() {
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

    const [activeFullEdgeIds, setActiveFullEdgeIds] = useState<string[]>([]);
    const [activeStubEdgeIds, setActiveStubEdgeIds] = useState<string[]>([]);
    const [currentWaveType, setCurrentWaveType] = useState<string | null>(null);
    const [currentCategory, setCurrentCategory] = useState<string | null>(null);

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

                if (payload.delta) {
                    setActiveFullEdgeIds(payload.delta.activeFullEdgeIds);
                    setActiveStubEdgeIds(payload.delta.activeStubEdgeIds);
                    setCurrentWaveType(payload.delta.waveType);
                    setCurrentCategory(payload.delta.category);
                } else {
                    setActiveFullEdgeIds([]);
                    setActiveStubEdgeIds([]);
                    setCurrentWaveType(null);
                }

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

    function activeEdgeColor(waveType: string | null) {
        switch (waveType) {
            case "SHEEP_TO_RELATIONSHIP":
                return "#22d3ee"; // cyan
            case "RELATIONSHIP_TO_SHEEP":
                return "#facc15"; // yellow/gold
            default:
                return "gold";
        }
    }

    const GRAPH_WIDTH = 700;
    const GRAPH_HEIGHT = 500;
    const CENTER_X = GRAPH_WIDTH / 2;
    const CENTER_Y = GRAPH_HEIGHT / 2;

    const graphScale = (() => {
        if (!graph || graph.nodes.length === 0) return 1;

        const maxX = Math.max(...graph.nodes.map((n) => Math.abs(n.x)));
        const maxY = Math.max(...graph.nodes.map((n) => Math.abs(n.y)));

        const paddingX = 70;
        const paddingY = 50;

        const usableHalfWidth = GRAPH_WIDTH / 2 - paddingX;
        const usableHalfHeight = GRAPH_HEIGHT / 2 - paddingY;

        const scaleX = maxX > 0 ? usableHalfWidth / maxX : 1;
        const scaleY = maxY > 0 ? usableHalfHeight / maxY : 1;

        return Math.min(1, scaleX, scaleY);
    })();

    const SCALE = 0.6;
    const LAYOUT_X_SCALE = 1.15;
    const LAYOUT_Y_SCALE = 0.80;

    function activeArrowMarker(waveType: string | null) {
        switch (waveType) {
            case "SHEEP_TO_RELATIONSHIP":
                return "url(#arrowhead-cyan)";
            case "RELATIONSHIP_TO_SHEEP":
                return "url(#arrowhead-gold)";
            default:
                return undefined;
        }
    }

    function quadraticPathWithLane(
        x1: number,
        y1: number,
        x2: number,
        y2: number,
        bend: number,
        laneOffset: number
    ) {
        const mx = (x1 + x2) / 2;
        const my = (y1 + y2) / 2;

        const dx = x2 - x1;
        const dy = y2 - y1;
        const len = Math.sqrt(dx * dx + dy * dy) || 1;

        const px = -dy / len;
        const py = dx / len;

        const cx = mx + px * (bend + laneOffset);
        const cy = my + py * (bend + laneOffset);

        return `M ${x1} ${y1} Q ${cx} ${cy} ${x2} ${y2}`;
    }

    function fanAngleOffset(index: number, count: number, maxSpreadRadians: number) {
        if (count <= 1) return 0;

        const start = -maxSpreadRadians / 2;
        const step = maxSpreadRadians / (count - 1);
        return start + index * step;
    }

    function rotateVector(x: number, y: number, angle: number) {
        const cos = Math.cos(angle);
        const sin = Math.sin(angle);
        return {
            x: x * cos - y * sin,
            y: x * sin + y * cos,
        };
    }

    function centeredFanOffset(index: number, count: number, maxOffset: number) {
        if (count <= 1) return 0;
        const midpoint = (count - 1) / 2;
        if (midpoint === 0) return 0;
        return ((index - midpoint) / midpoint) * maxOffset;
    }

    function quadraticPathWithControl(
        x1: number,
        y1: number,
        x2: number,
        y2: number,
        cx: number,
        cy: number
    ) {
        return `M ${x1} ${y1} Q ${cx} ${cy} ${x2} ${y2}`;
    }

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
                            <div className="overflow-auto max-w-full max-h-[80vh]">
                                {(() => {
                                    const nodeSize = (type: "sheep" | "relationship") =>
                                        type === "relationship"
                                            ? { width: 150 * SCALE, height: 70 * SCALE }
                                            : { width: 140 * SCALE, height: 70 * SCALE };

                                    const rawNodes = graph.nodes.map((n) => {
                                        const size = nodeSize(n.type);

                                        return {
                                            ...n,
                                            ...size,
                                            rawCx: n.x * SCALE * LAYOUT_X_SCALE,
                                            rawCy: n.y * SCALE * LAYOUT_Y_SCALE,
                                        };
                                    });

                                    const minX = Math.min(...rawNodes.map((n) => n.rawCx - n.width / 2));
                                    const maxX = Math.max(...rawNodes.map((n) => n.rawCx + n.width / 2));
                                    const minY = Math.min(...rawNodes.map((n) => n.rawCy - n.height / 2));
                                    const maxY = Math.max(...rawNodes.map((n) => n.rawCy + n.height / 2));

                                    const paddingX = 120;
                                    const paddingY = 120;

                                    const svgWidth = Math.max(900, maxX - minX + paddingX * 2);
                                    const svgHeight = Math.max(700, maxY - minY + paddingY * 2);

                                    const offsetX = paddingX - minX;
                                    const offsetY = paddingY - minY;

                                    const nodeMap = new Map(
                                        rawNodes.map((n) => [
                                            n.id,
                                            {
                                                ...n,
                                                cx: n.rawCx + offsetX,
                                                cy: n.rawCy + offsetY,
                                            },
                                        ])
                                    );

                                    return (
                                        <svg
                                            width={svgWidth}
                                            height={svgHeight}
                                            viewBox={`0 0 ${svgWidth} ${svgHeight}`}
                                            className="rounded border border-gray-700 bg-gray-900"
                                        >
                                            <defs>
                                                <marker
                                                    id="arrowhead-cyan"
                                                    markerWidth="8"
                                                    markerHeight="8"
                                                    refX="6"
                                                    refY="4"
                                                    orient="auto-start-reverse"
                                                    markerUnits="userSpaceOnUse"
                                                >
                                                    <path d="M0,0 L0,8 L8,4 z" fill="#22d3ee" />
                                                </marker>

                                                <marker
                                                    id="arrowhead-gold"
                                                    markerWidth="8"
                                                    markerHeight="8"
                                                    refX="6"
                                                    refY="4"
                                                    orient="auto-start-reverse"
                                                    markerUnits="userSpaceOnUse"
                                                >
                                                    <path d="M0,0 L0,8 L8,4 z" fill="#facc15" />
                                                </marker>
                                            </defs>

                                            <>
                                                {/* Edges */}
                                                {graph.edges.map((edge) => {
                                                    const source = nodeMap.get(edge.sourceId);
                                                    const target = nodeMap.get(edge.targetId);
                                                    const highlightColor = activeEdgeColor(currentWaveType);

                                                    if (!source) return null;

                                                    // ---------- FULL EDGES ----------
                                                    if (edge.type === "full") {
                                                        if (!target) return null;

                                                        const reverseId = (() => {
                                                            const [a, b] = edge.id.split("--");
                                                            return `${b}--${a}`;
                                                        })();

                                                        const isActive =
                                                            activeFullEdgeIds.includes(edge.id) ||
                                                            activeFullEdgeIds.includes(reverseId);

                                                        const isOuterFullEdge =
                                                            (source.type === "relationship" && target.type === "sheep" && !target.center) ||
                                                            (source.type === "sheep" && !source.center && target.type === "relationship");

                                                        // Stable geometry orientation:
                                                        // always relationship -> sheep for mixed sheep/relationship full edges
                                                        let geomFrom = source;
                                                        let geomTo = target;

                                                        if (
                                                            (source.type === "relationship" && target.type === "sheep") ||
                                                            (source.type === "sheep" && target.type === "relationship")
                                                        ) {
                                                            if (source.type === "sheep") {
                                                                geomFrom = target;
                                                                geomTo = source;
                                                            }
                                                        }

                                                        const dx = geomTo.cx - geomFrom.cx;
                                                        const dy = geomTo.cy - geomFrom.cy;
                                                        const len = Math.sqrt(dx * dx + dy * dy) || 1;

                                                        let ux = dx / len;
                                                        let uy = dy / len;

                                                        // subtle fan-out for sibling outer full edges
                                                        if (isOuterFullEdge && edge.stubIndex != null && edge.stubCount != null) {
                                                            const offset = fanAngleOffset(edge.stubIndex, edge.stubCount, 0.35);
                                                            const rotated = rotateVector(ux, uy, offset);
                                                            ux = rotated.x;
                                                            uy = rotated.y;
                                                        }

                                                        const fromExit = rectExitOffset(
                                                            ux,
                                                            uy,
                                                            geomFrom.width / 2,
                                                            geomFrom.height / 2,
                                                            2
                                                        );

                                                        const toExit = rectExitOffset(
                                                            -ux,
                                                            -uy,
                                                            geomTo.width / 2,
                                                            geomTo.height / 2,
                                                            6
                                                        );

                                                        let x1 = geomFrom.cx + fromExit.dx;
                                                        let y1 = geomFrom.cy + fromExit.dy;
                                                        let x2 = geomTo.cx + toExit.dx;
                                                        let y2 = geomTo.cy + toExit.dy;

                                                        // Active arrow direction depends on wave, but geometry does not
                                                        let markerStart: string | undefined = undefined;
                                                        let markerEnd: string | undefined = undefined;

                                                        if (isActive && currentWaveType) {
                                                            if (currentWaveType === "RELATIONSHIP_TO_SHEEP") {
                                                                markerEnd = activeArrowMarker(currentWaveType);
                                                            } else if (currentWaveType === "SHEEP_TO_RELATIONSHIP") {
                                                                markerStart = activeArrowMarker(currentWaveType);
                                                            }
                                                        }

                                                        if (isOuterFullEdge) {
                                                            let laneOffset = 0;

                                                            if (edge.stubIndex != null && edge.stubCount != null) {
                                                                laneOffset = centeredFanOffset(edge.stubIndex, edge.stubCount, 22 * SCALE);
                                                            }

                                                            const px = -uy;
                                                            const py = ux;

                                                            const sourceLaneOffset = laneOffset * 0.45;
                                                            const targetLaneOffset = laneOffset * 1.35;

                                                            x1 += px * sourceLaneOffset;
                                                            y1 += py * sourceLaneOffset;
                                                            x2 += px * targetLaneOffset;
                                                            y2 += py * targetLaneOffset;

                                                            const dxCurve = x2 - x1;
                                                            const dyCurve = y2 - y1;
                                                            const edgeLength = Math.sqrt(dxCurve * dxCurve + dyCurve * dyCurve) || 1;

                                                            const bendMagnitude = Math.max(
                                                                24 * SCALE,
                                                                Math.min(edgeLength * 0.22, 90 * SCALE)
                                                            );

                                                            const mx = (x1 + x2) / 2;
                                                            const my = (y1 + y2) / 2;

// choose the perpendicular direction that bends farther away from the graph center
                                                            const candidate1X = mx + px * bendMagnitude;
                                                            const candidate1Y = my + py * bendMagnitude;

                                                            const candidate2X = mx - px * bendMagnitude;
                                                            const candidate2Y = my - py * bendMagnitude;

// graph center in the current translated SVG space
                                                            const centerNode = Array.from(nodeMap.values()).find((n) => n.center);
                                                            const graphCenterX = centerNode ? centerNode.cx : 0;
                                                            const graphCenterY = centerNode ? centerNode.cy : 0;

                                                            const dist1 =
                                                                (candidate1X - graphCenterX) * (candidate1X - graphCenterX) +
                                                                (candidate1Y - graphCenterY) * (candidate1Y - graphCenterY);

                                                            const dist2 =
                                                                (candidate2X - graphCenterX) * (candidate2X - graphCenterX) +
                                                                (candidate2Y - graphCenterY) * (candidate2Y - graphCenterY);

// pick the side that is farther from the graph center
                                                            const outwardSign = dist1 >= dist2 ? 1 : -1;

                                                            const cx = mx + px * (laneOffset + outwardSign * bendMagnitude);
                                                            const cy = my + py * (laneOffset + outwardSign * bendMagnitude);

                                                            const pathD = quadraticPathWithControl(x1, y1, x2, y2, cx, cy);

                                                            return (
                                                                <path
                                                                    key={edge.id}
                                                                    d={pathD}
                                                                    fill="none"
                                                                    stroke={isActive ? highlightColor : "rgba(156, 163, 175, 0.45)"}
                                                                    strokeWidth={isActive ? 2.5 : 2.0}
                                                                    opacity={isActive ? 1 : 0.45}
                                                                    markerStart={markerStart}
                                                                    markerEnd={markerEnd}
                                                                />
                                                            );
                                                        }

                                                        return (
                                                            <line
                                                                key={edge.id}
                                                                x1={x1}
                                                                y1={y1}
                                                                x2={x2}
                                                                y2={y2}
                                                                stroke={isActive ? highlightColor : "rgba(156, 163, 175, 0.45)"}
                                                                strokeWidth={isActive ? 2.5 : 2.0}
                                                                opacity={isActive ? 1 : 0.45}
                                                                markerStart={markerStart}
                                                                markerEnd={markerEnd}
                                                            />
                                                        );
                                                    }

                                                    // ---------- STUB EDGES ----------
                                                    const angle = edge.stubAngleRadians ?? 0;

                                                    // outward from displayed node toward hidden node
                                                    const outwardUx = Math.cos(angle);
                                                    const outwardUy = Math.sin(angle);

                                                    const isActive = activeStubEdgeIds.includes(edge.id);

                                                    // Message goes outward if it originates at the displayed node.
                                                    const isOutwardMessage =
                                                        !isActive ||
                                                        (source.type === "relationship"
                                                            ? currentWaveType === "RELATIONSHIP_TO_SHEEP"
                                                            : currentWaveType === "SHEEP_TO_RELATIONSHIP");

                                                    // boundary point on the outward-facing side of the displayed node
                                                    const outwardExit = rectExitOffset(
                                                        outwardUx,
                                                        outwardUy,
                                                        source.width / 2,
                                                        source.height / 2,
                                                        6
                                                    );

                                                    const boundaryX = source.cx + outwardExit.dx;
                                                    const boundaryY = source.cy + outwardExit.dy;

                                                    const stubLength = 65 * SCALE;

                                                    let x1: number, y1: number, x2: number, y2: number;

                                                    if (isOutwardMessage) {
                                                        // displayed node -> hidden node
                                                        x1 = boundaryX;
                                                        y1 = boundaryY;
                                                        x2 = boundaryX + outwardUx * stubLength;
                                                        y2 = boundaryY + outwardUy * stubLength;
                                                    } else {
                                                        // hidden node -> displayed node
                                                        x1 = boundaryX + outwardUx * stubLength;
                                                        y1 = boundaryY + outwardUy * stubLength;
                                                        x2 = boundaryX;
                                                        y2 = boundaryY;
                                                    }

                                                    return (
                                                        <g key={edge.id}>
                                                            <line
                                                                x1={x1}
                                                                y1={y1}
                                                                x2={x2}
                                                                y2={y2}
                                                                stroke={isActive ? highlightColor : "rgba(156, 163, 175, 0.45)"}
                                                                strokeWidth={isActive ? 3.5 : 2.5}
                                                                strokeDasharray="8 6"
                                                                strokeLinecap="round"
                                                                opacity={isActive ? 1 : 0.8}
                                                                markerEnd={isActive ? activeArrowMarker(currentWaveType) : undefined}
                                                            />

                                                            {(!isActive || !isOutwardMessage) && (
                                                                <circle
                                                                    cx={isOutwardMessage ? x2 : x1}
                                                                    cy={isOutwardMessage ? y2 : y1}
                                                                    r={isActive ? 6 : 5}
                                                                    fill={isActive ? highlightColor : "rgba(156, 163, 175, 0.45)"}
                                                                />
                                                            )}
                                                        </g>
                                                    );
                                                })}

                                                {/* Nodes */}
                                                {Array.from(nodeMap.values()).map((node) => {
                                                    const x = node.cx - node.width / 2;
                                                    const y = node.cy - node.height / 2;
                                                    const fill =
                                                        node.type === "relationship"
                                                            ? "#a000e8"
                                                            : "#2563eb";
                                                    const stroke = node.center
                                                        ? "#93c5fd"
                                                        : "#d8b4fe";

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
                                                                fontSize={18 * SCALE}
                                                                textAnchor="middle"
                                                            >
                                                                {node.label}
                                                            </text>
                                                            <text
                                                                x={node.cx}
                                                                y={node.cy + 20 * SCALE}
                                                                fill="rgba(255,255,255,0.7)"
                                                                fontSize={12 * SCALE}
                                                                textAnchor="middle"
                                                            >
                                                                {node.id}
                                                            </text>
                                                        </g>
                                                    );
                                                })}
                                            </>
                                        </svg>
                                    );
                                })()}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
