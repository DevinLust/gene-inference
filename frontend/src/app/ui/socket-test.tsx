"use client";

import { useEffect, useRef, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { createClient as createSupabaseClient } from "@/app/lib/supabase/browser";
import { Category } from "@/app/lib/definitions";

type RunEventType = "RUN_STARTED" | "STEP_EVENT" | "COMPLETED";
type RunStage = "MESSAGE_PASSING" | "BELIEF_UPDATE" | "COMPLETED";

type RunStartedPayload = {
    totalSteps: number;
    currentStep: number;
    stage: RunStage;
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
                    className="rounded border boarder-gray-500 px-3 py-2 text-white"
                >
                    {CATEGORIES.map((c) => (
                        <option key={c} value={c}>
                            {c}
                        </option>
                    ))}
                </select>
                <button
                    onClick={startRun}
                    disabled={!isConnected && targetSheepId == null}
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
