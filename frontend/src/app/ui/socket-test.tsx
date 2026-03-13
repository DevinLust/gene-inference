"use client";

import { useEffect, useRef, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import { createClient as createSupabaseClient } from "@/app/lib/supabase/browser";

type RunEvent = {
    type: string;
    runId: string;
    payload: any;
};

export default function SocketTest() {
    const stompRef = useRef<Client | null>(null);
    const [events, setEvents] = useState<RunEvent[]>([]);
    const [isConnected, setIsConnected] = useState(false);
    const [accessToken, setAccessToken] = useState<string | null>(null);

    useEffect(() => {
        const supabase = createSupabaseClient();

        const loadInitialSession = async () => {
            const { data, error } = await supabase.auth.getSession();

            if (error) {
                console.error("Failed to get Supabase session:", error);
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
            console.log("Connected to WebSocket");
            setIsConnected(true);

            client.subscribe("/user/queue/run-events", (message: IMessage) => {
                const body: RunEvent = JSON.parse(message.body);
                setEvents((prev) => [...prev, body]);
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
            console.error("Broker error:", frame.headers["message"], frame.body);
        };

        client.activate();
        stompRef.current = client;

        return () => {
            setIsConnected(false);
            client.deactivate();
        };
    }, [accessToken]);

    const startRun = () => {
        const client = stompRef.current;
        if (!client || !client.connected) return;

        client.publish({
            destination: "/app/run.start",
            body: JSON.stringify({
                graphId: "test-graph",
                focusSheepId: "sheep-1",
            }),
        });
    };

    return (
        <div style={{ padding: 20 }}>
            <button onClick={startRun} disabled={!isConnected}>
                {isConnected ? "Start Run" : "Connecting..."}
            </button>

            <h3>Events</h3>
            {events.map((event, i) => (
                <pre key={i}>{JSON.stringify(event, null, 2)}</pre>
            ))}
        </div>
    );
}
