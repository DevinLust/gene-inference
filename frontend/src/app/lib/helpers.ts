import {GraphEntry} from "../ui/graph-nav";

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
