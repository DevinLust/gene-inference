
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
