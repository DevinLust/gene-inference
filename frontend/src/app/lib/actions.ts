'use server'; // server actions

import { Grade, Category, SheepCreateDTO } from '@/app/lib/definitions';
import { revalidatePath } from 'next/cache';
import { redirect } from 'next/navigation';

export type CreateState = {
    message?: string | null;
    errors?: {
        name?: string[];
        distributions?: string[];
        genotypes?: string[];
        parentRelationshipId?: string[];
    };
};

export type BreedState = {
    message?: string | null;
    errors?: {
        sheep1Id?: string;
        sheep2Id?: string;
    };
};

if (!process.env.NEXT_PUBLIC_API_BASE_URL) {
  throw new Error("NEXT_PUBLIC_API_BASE_URL is not defined");
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

const categories: Category[] = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];
const grades: Grade[] = ["S", "A", "B", "C", "D", "E"];

export async function formDataToSheepDTO(formData: FormData): Promise<SheepCreateDTO> {
    // Build genotypes
    const genotypes = Object.fromEntries(
        categories.map((c) => [
            c,
            {
                phenotype: formData.get(`genotypes.${c}.phenotype`) as Grade || null,
                hiddenAllele: (formData.get(`genotypes.${c}.hiddenAllele`) as Grade) || null,
            },
        ])
    ) as SheepCreateDTO["genotypes"];

    // Optionally parse distributions if the user included them in the form
    const distributions: SheepCreateDTO["distributions"] = Object.fromEntries(
        categories
            .map((c) => {
                // Build grade -> number map for this category
                const gradeMap: Record<Grade, number> = {} as Record<Grade, number>;

                let hasValue = false;
                for (const g of grades) {
                    const val = formData.get(`distributions.${c}.${g}`);
                    if (val !== null && val !== "") {
                        gradeMap[g] = Number(val);
                        hasValue = true;
                    } else {
                        gradeMap[g] = 0; // fill missing with 0
                    }
                }

                // Only include category if at least one grade was filled
                if (hasValue) return [c, gradeMap];
                return null;
            })
            .filter(Boolean) as [Category, Record<Grade, number>][]
    );

    const parentRelationshipIdRaw = formData.get("parentRelationshipId");
    const parentRelationshipId =
        parentRelationshipIdRaw === null || parentRelationshipIdRaw === ""
            ? null
            : (() => {
                const n = Number(parentRelationshipIdRaw);
                if (Number.isNaN(n)) {
                    throw new Error("Invalid parentRelationshipId");
                }
                return n;
            })();

    return {
        name: formData.get("name") as string,
        genotypes,
        distributions: distributions,
        parentRelationshipId: parentRelationshipId,
    };
}

export async function createSheep(prevState: CreateState, formData: FormData) {
    let newSheep: SheepCreateDTO;
    try {
        newSheep = await formDataToSheepDTO(formData);
    } catch (err) {
        return err instanceof Error && err.message ? { message: err.message, errors: {} } : { message: "Invalid form data", errors: {} };
    }

    const res = await fetch(`${API_BASE_URL}/sheep`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newSheep),
    });

    if (!res.ok) {
        return await parseError(res);
    }


    revalidatePath('/sheep');
    redirect('/sheep');
}

export async function breedSheep(prevState: BreedState, formData: FormData) {
    const parent1Id = formData.get("parent1Id") as string;
    const parent2Id = formData.get("parent2Id") as string;

    if (!parent1Id || !parent2Id || isNaN(Number(parent1Id)) || isNaN(Number(parent2Id))) {
        return { message: "Both parent Ids must be valid numbers", errors: {} };
    }

    const res = await fetch(`${API_BASE_URL}/breed/${parent1Id}/${parent2Id}`, {
        method: "POST",
        headers: {"Content-Type": "application/json" },
    });

    if (!res.ok) {
        return await parseError(res);
    }

    revalidatePath('/sheep');
    redirect('/sheep');
}

async function parseError(res: Response) {
    try {
        const contentType = res.headers.get("content-type");

        if (contentType?.includes("application/json")) {
            return await res.json(); // ← return full object
        }

        return { message: await res.text() };
    } catch {
        return { message: "An unexpected error occurred" };
    }
}


