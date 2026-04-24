'use server'; // server actions

import { Grade, Category, SheepCreateDTO, SheepUpdateDTO, SheepChildDTO, BirthRecord, ValidationFailed, GeneticConstraintViolation, ALL_CATEGORIES } from '@/app/lib/definitions';
import { revalidatePath } from 'next/cache';
import { redirect } from 'next/navigation';
import { createClient } from "@/app/lib/supabase/server";

export type CreateState = {
    message?: string | null;
    errors?: {
        name?: string[];
        genotypes?: Partial<Record<Category, string[]>>;
    };
};

export type ChildState =
    | { status: "idle" }                                  // initial
    | { status: "success"; ok: true; message?: string }    // optional
    | ({ status: "error" } & ValidationFailed)             // backend validation
    | ({ status: "error" } & GeneticConstraintViolation);  // backend genetic constraint

export type UpdateSheepState =
    | { status: "idle" }
    | { status: "success"; ok: true; message?: string }
    | ({ status: "error" } & ValidationFailed)
    | ({ status: "error" } & GeneticConstraintViolation);

export type BreedState = {
    message?: string | null;
    errors?: {
        parent1MissingCategories?: string;
        parent2MissingCategories?: string;
    };
};

if (!process.env.NEXT_PUBLIC_API_BASE_URL) {
  throw new Error("NEXT_PUBLIC_API_BASE_URL is not defined");
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

const grades: Grade[] = ["S", "A", "B", "C", "D", "E"];

export async function logout(formData: FormData, nextPath: string = "/login") {
    const supabase = await createClient();
    await supabase.auth.signOut();
    redirect(nextPath);
}

function parseOptionalString(value: FormDataEntryValue | null): string | null {
    if (typeof value !== "string") return null;
    const trimmed = value.trim();
    return trimmed === "" ? null : trimmed;
}

function parseOptionalNumber(value: FormDataEntryValue | null): number | null {
    if (value === null || value === "") return null;
    const n = Number(value);
    if (Number.isNaN(n)) {
        throw new Error("Invalid numeric value");
    }
    return n;
}

function formDataToGenotypes(
    formData: FormData
): SheepCreateDTO["genotypes"] | null {
    let hasAnyPhenotype = false;

    const result = Object.fromEntries(
        ALL_CATEGORIES.map((c) => {
            const phenotype = parseOptionalString(
                formData.get(`genotypes.${c}.phenotype`)
            );
            const hiddenAllele = parseOptionalString(
                formData.get(`genotypes.${c}.hiddenAllele`)
            );

            if (phenotype !== null) {
                hasAnyPhenotype = true;
            }

            return [
                c,
                {
                    phenotype,
                    hiddenAllele,
                },
            ];
        })
    ) as SheepCreateDTO["genotypes"];

    return hasAnyPhenotype ? result : null;
}

export async function formDataToSheepCreateDTO(
    formData: FormData
): Promise<SheepCreateDTO> {
    const genotypes = formDataToGenotypes(formData);

    const name = parseOptionalString(formData.get("name"));

    return {
        name,
        genotypes,
    };
}

export async function formDataToSheepUpdateDTO(
    formData: FormData
): Promise<SheepUpdateDTO> {
    return {
        name: parseOptionalString(formData.get("name")),
        genotypes: formDataToGenotypes(formData),
    };
}

export async function formDataToChildDTO(
    formData: FormData
): Promise<SheepChildDTO> {
    const name = parseOptionalString(formData.get("name"));

    const parent1Id = parseOptionalNumber(formData.get("parent1Id"));
    const parent2Id = parseOptionalNumber(formData.get("parent2Id"));

    if (parent1Id === null || parent2Id === null) {
        throw new Error("Both parent IDs are required");
    }

    return {
        name,
        genotypes: formDataToGenotypes(formData),
        parent1Id,
        parent2Id,
    };
}

export async function createSheep(prevState: CreateState, formData: FormData) {
    let newSheep: SheepCreateDTO;
    try {
        newSheep = await formDataToSheepCreateDTO(formData);
    } catch (err) {
        return err instanceof Error && err.message ? { message: err.message, errors: {} } : { message: "Invalid form data", errors: {} };
    }

    const res = await fetch(`${API_BASE_URL}/sheep`, {
        method: "POST",
        headers: await authHeaders(),
        body: JSON.stringify(newSheep),
    });

    if (!res.ok) {
        return await parseError(res);
    }


    revalidatePath('/sheep');
    redirect('/sheep');
}

export async function recordChild(prevState: ChildState, formData: FormData): Promise<ChildState> {
    const parent1Id = formData.get("parent1Id") as string;
    const parent2Id = formData.get("parent2Id") as string;

    if (!parent1Id || !parent2Id || isNaN(Number(parent1Id)) || isNaN(Number(parent2Id))) {
        return {
            status: "error",
            ok: false,
            error: "VALIDATION_FAILED",
            message: "Incomplete or invalid request",
            errors: {
                parent1Id: !parent1Id ? ["Parent 1 must be selected"] : undefined,
                parent2Id: !parent2Id ? ["Parent 2 must be selected"] : undefined,
            },
        };
    }
    if (parent1Id === parent2Id) {
        return {
            status: "error",
            ok: false,
            error: "VALIDATION_FAILED",
            message: "Parents must be different.",
        };
    }

    let newChild: SheepChildDTO;
    try {
        newChild = await formDataToChildDTO(formData);
    } catch (err) {
        return {
            status: "error",
            ok: false,
            error: "VALIDATION_FAILED",
            message: err instanceof Error && err.message ? err.message : "Invalid form data",
        };
    }

    const saveChild = formData.get("saveChild") === "on";
    const url = new URL(`${API_BASE_URL}/breed/record-birth`);
    url.searchParams.set("saveChild", String(saveChild));

    const res = await fetch(url.toString(), {
        method: "POST",
        headers: await authHeaders(),
        body: JSON.stringify(newChild),
    });

    if (!res.ok) {
        const apiErr: ValidationFailed | GeneticConstraintViolation = await parseError(res);
        // Wrap it with status:"error" so narrowing works everywhere
        return {
            status: "error",
            ok: false,
            ...apiErr,
        } as ChildState;
    }

    const birthRecord = await res.json() as BirthRecord;

    revalidatePath('/sheep');
    redirect(`/birth-record/${birthRecord.id}`);
}

export async function breedSheep(prevState: BreedState, formData: FormData) {
    const parent1Id = formData.get("parent1Id") as string;
    const parent2Id = formData.get("parent2Id") as string;

    if (!parent1Id || !parent2Id || isNaN(Number(parent1Id)) || isNaN(Number(parent2Id))) {
        return { message: "Both parents must be selected" };
    }

    const saveChild = formData.get("saveChild") === "on";
    const childNameRaw = formData.get("name") as string | null;

    const childName =
        childNameRaw && childNameRaw.trim() !== ""
            ? childNameRaw.trim()
            : null;

    const params = new URLSearchParams({
        saveChild: String(saveChild),
    });

    if (childName) {
        params.set("name", childName);
    }

    const res = await fetch(`${API_BASE_URL}/breed/${parent1Id}/${parent2Id}?${params.toString()}`, {
        method: "POST",
        headers: await authHeaders(),
    });

    if (!res.ok) {
        return await parseError(res);
    }

    const birthRecord = await res.json() as BirthRecord;

    revalidatePath('/sheep');
    redirect(`/birth-record/${birthRecord.id}`);
}

export async function updateSheep(
    sheepId: number,
    prevState: UpdateSheepState,
    formData: FormData
): Promise<UpdateSheepState> {
    let updateDTO: SheepUpdateDTO;

    try {
        updateDTO = await formDataToSheepUpdateDTO(formData);
    } catch (err) {
        return {
            status: "error",
            error: "VALIDATION_FAILED",
            message: err instanceof Error && err.message ? err.message : "Invalid form data",
            errors: {},
        };
    }

    const res = await fetch(`${API_BASE_URL}/sheep/${sheepId}`, {
        method: "PATCH",
        headers: await authHeaders(),
        body: JSON.stringify(updateDTO),
    });

    if (!res.ok) {
        const apiErr = await parseError(res);
        // Wrap it with status:"error" so narrowing works everywhere
        return {
            status: "error",
            ...apiErr,
        } as UpdateSheepState;
    }

    revalidatePath("/sheep");
    revalidatePath(`/sheep/${sheepId}`);
    redirect(`/sheep/${sheepId}`);
}

export async function updateSheepName(
    sheepId: number,
    prevState: UpdateSheepState,
    formData: FormData
): Promise<UpdateSheepState> {
    let updateDTO: SheepUpdateDTO;

    try {
        updateDTO = await formDataToSheepUpdateDTO(formData);
    } catch (err) {
        return {
            status: "error",
            error: "VALIDATION_FAILED",
            message: err instanceof Error && err.message ? err.message : "Invalid form data",
            errors: {},
        };
    }

    if (!updateDTO.name) {
        return {
            status: "error",
            error: "VALIDATION_FAILED",
            message: "Name is required",
            errors: { name: ["Name cannot be empty"] },
        };
    }

    const res = await fetch(`${API_BASE_URL}/sheep/${sheepId}`, {
        method: "PATCH",
        headers: await authHeaders(),
        body: JSON.stringify(updateDTO),
    });

    if (!res.ok) {
        return await parseError(res);
    }

    revalidatePath("/sheep");
    revalidatePath(`/sheep/${sheepId}`);

    return {
        status: "success",
        ok: true,
        message: "Sheep updated successfully",
    };
}


export async function evolveSheep(sheepId: number, category: Category) {
    const res = await fetch(`${API_BASE_URL}/sheep/${sheepId}/evolve/${category}`, {
        method: "POST",
        headers: await authHeaders(),
    });

    if (!res.ok) {
        return await parseError(res);
    }

    revalidatePath(`/sheep/${sheepId}`);
    return { success: true };
}


export async function recalculateBeliefs() {
    const res = await fetch(`${API_BASE_URL}/breed/recalculate-beliefs`, {
        method: "POST",
        headers: await authHeaders(),
    })

    if (!res.ok) {
        return await parseError(res);
    }

    revalidatePath('/sheep');
    //redirect('/sheep');
}


export async function deleteSheep(
    sheepId: number,
    prevState: { success?: boolean, errors?: string[] },
    formData: FormData
) {
    const res = await fetch(`${API_BASE_URL}/sheep/${sheepId}`, {
        method: "DELETE",
        headers: await authHeaders(),
    });

    if (!res.ok) {
        return await parseError(res);
    }

    revalidatePath('/sheep');
    redirect("/sheep");
}

export async function deleteBirthRecord(
    brId: number,
    prevState: { success?: boolean, errors?: string[] },
    formData: FormData
) {
    const res = await fetch(`${API_BASE_URL}/birth-record/${brId}`, {
        method: "DELETE",
        headers: await authHeaders(),
    });

    if (!res.ok) {
        return await parseError(res);
    }

    revalidatePath('/birth-record');
    redirect("/birth-record");
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

async function authHeaders(): Promise<Record<string, string>> {
    const supabase = await createClient();

    const {
        data: { session },
    } = await supabase.auth.getSession();

    if (!session) {
        throw new Error("User not authenticated");
    }

    return {
        Authorization: `Bearer ${session.access_token}`,
        "Content-Type": "application/json",
    };
}


