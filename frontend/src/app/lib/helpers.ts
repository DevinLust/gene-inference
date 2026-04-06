import {GraphEntry} from "../ui/graph-nav";
import { RunStage } from '@/app/lib/definitions';

export function buildQuery(paramsObj: Record<string, unknown>) {
    const params = new URLSearchParams();

    for (const [key, value] of Object.entries(paramsObj)) {
        if (value === undefined || value === null || value === "") continue;

        if (Array.isArray(value)) {
            for (const v of value) {
                params.append(key, String(v));
            }
        } else {
            params.set(key, String(value));
        }
    }

    return params.toString();
}

export function detectNode(pathname: string): GraphEntry | null {
    const parts = pathname.split("/");

    if (parts[1] === "sheep" && parts[2]) {
        return {
            type: "sheep",
            id: parts[2],
            href: pathname,
        };
    }

    if (parts[1] === "relationship" && parts[2]) {
        return {
            type: "relationship",
            id: parts[2],
            href: pathname,
        };
    }

    return null;
}


// Loopy belief visualization helpers
export function rectExitOffset(
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

export function activeEdgeColor(
    stage: RunStage | "IDLE",
    waveType: string | null,
    relationshipRole?: "PARENT" | "CHILD" | null
) {
    if (stage === "BELIEF_UPDATE") {
        return relationshipRole === "CHILD" ? "#14b8a6" : "#84cc16";
    }

    if (waveType === "SHEEP_TO_RELATIONSHIP") {
        return relationshipRole === "CHILD" ? "#a78bfa" : "#22d3ee";
    }

    if (waveType === "RELATIONSHIP_TO_SHEEP") {
        return relationshipRole === "CHILD" ? "#fb7185" : "#facc15";
    }

    return "gold";
}

export function activeArrowMarker(
    stage: RunStage | "IDLE",
    waveType: string | null,
    relationshipRole?: "PARENT" | "CHILD" | null
) {
    if (stage === "BELIEF_UPDATE") {
        return relationshipRole === "CHILD"
            ? "url(#arrowhead-teal)"
            : "url(#arrowhead-lime)";
    }

    if (waveType === "SHEEP_TO_RELATIONSHIP") {
        return relationshipRole === "CHILD"
            ? "url(#arrowhead-purple)"
            : "url(#arrowhead-cyan)";
    }

    if (waveType === "RELATIONSHIP_TO_SHEEP") {
        return relationshipRole === "CHILD"
            ? "url(#arrowhead-red)"
            : "url(#arrowhead-gold)";
    }

    return undefined;
}

export function fanAngleOffset(index: number, count: number, maxSpreadRadians: number) {
    if (count <= 1) return 0;

    const start = -maxSpreadRadians / 2;
    const step = maxSpreadRadians / (count - 1);
    return start + index * step;
}

export function rotateVector(x: number, y: number, angle: number) {
    const cos = Math.cos(angle);
    const sin = Math.sin(angle);
    return {
        x: x * cos - y * sin,
        y: x * sin + y * cos,
    };
}

export function centeredFanOffset(index: number, count: number, maxOffset: number) {
    if (count <= 1) return 0;
    const midpoint = (count - 1) / 2;
    if (midpoint === 0) return 0;
    return ((index - midpoint) / midpoint) * maxOffset;
}

export function quadraticPathWithControl(
    x1: number,
    y1: number,
    x2: number,
    y2: number,
    cx: number,
    cy: number
) {
    return `M ${x1} ${y1} Q ${cx} ${cy} ${x2} ${y2}`;
}
