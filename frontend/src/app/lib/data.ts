'use server';

import { createClient } from "@/app/lib/supabase/server";
import { Sheep, Prediction, BestPrediction, Relationship, RelationshipRow, BirthRecord, BirthRecordRow, BirthRecordFilter, DistributionFilter, SheepFilter, PageResponse } from "./definitions";
import { BreedState } from "./actions";
import { buildQuery } from "./helpers";
import { notFound, redirect } from "next/navigation";

if (!process.env.NEXT_PUBLIC_API_BASE_URL) {
  throw new Error("NEXT_PUBLIC_API_BASE_URL is not defined");
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

// data fetching functions
export async function fetchAllSheep(filter: SheepFilter): Promise<Sheep[]> {
    const query = buildQuery(filter);
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/sheep?${query}`, {
        headers: headers,
        cache: "default",
    });

    await checkStatus(res);

    return await res.json();
}

export async function fetchDistributions(filter: DistributionFilter) {
    const query = buildQuery(filter);
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/sheep/distributions?${query}`, {
        headers: headers,
    });

    await checkStatus(res);

    return await res.json();
}

export async function fetchSheepById(id: string): Promise<Sheep> {
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/sheep/${id}`, {
        headers: headers,
    });

    await checkStatus(res);

    return await res.json() as Promise<Sheep>;
}

export async function fetchPrediction(
    sheep1Id: string,
    sheep2Id: string
): Promise<Prediction | BreedState> {
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/breed/${sheep1Id}/${sheep2Id}/predict`, {
        headers: headers,
    });
    const data = await res.json();

    if (!res.ok) return data as BreedState;
    return data as Prediction;
}

// both prediction fetches need to return objects for inline error handling
export async function fetchBestPredictions(): Promise<BestPrediction[] | BreedState> {
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/breed/best-predictions`, {
        headers: headers,
    });
    const data = await res.json();

    if (!res.ok) return data as BreedState;
    return data as BestPrediction[];
}

export async function fetchAllRelationships(): Promise<RelationshipRow[]> {
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/relationship`, {
        headers: headers,
    });

    await checkStatus(res);

    return await res.json() as RelationshipRow[];
}

export async function fetchRelationshipById(id: string): Promise<Relationship> {
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/relationship/${id}`, {
        headers: headers,
    });

    await checkStatus(res);

    return await res.json() as Relationship;
}

export async function fetchBirthRecordRows(
    filter: BirthRecordFilter
): Promise<PageResponse<BirthRecordRow>> {

    const query = buildQuery(filter);
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/birth-record?${query}`, {
        headers: headers,
        cache: "no-store"
    });

    await checkStatus(res);

    const data = await res.json();

    return data as PageResponse<BirthRecordRow>;
}

export async function fetchBirthRecordById(id: string): Promise<BirthRecord> {
    const headers = await authHeaders();

    const res = await fetch(`${API_BASE_URL}/birth-record/${id}`, {
        headers: headers,
    });

    await checkStatus(res);

    return await res.json() as BirthRecord;
}

async function checkStatus(res: Response) {
    if (res.status === 404) {
        notFound();
    }
    if (!res.ok) {
        let errorMessage = res.statusText;

        try {
            const data = await res.clone().json();
            // support a few common shapes
            errorMessage =
                (data?.message as string) ??
                (data?.msg as string) ??
                (data?.error as string) ??
                JSON.stringify(data);
        } catch {
            try {
                errorMessage = await res.clone().text();
            } catch {
                // ignore
            }
        }

        throw new Error(`Failed to fetch data: ${res.status} - ${errorMessage}`);
    }
}

async function authHeaders() {
    const supabase = await createClient();

    const {
        data: { session },
    } = await supabase.auth.getSession();

    if (!session) {
        redirect("/login");
    }

    return {
        Authorization: `Bearer ${session.access_token}`,
    };
}
