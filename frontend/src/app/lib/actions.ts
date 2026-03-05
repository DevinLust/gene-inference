'use server'; // server actions

import { Grade, Category, SheepCreateDTO, SheepUpdateDTO, SheepChildDTO, BirthRecord, ValidationFailed, GeneticConstraintViolation } from '@/app/lib/definitions';
import { revalidatePath } from 'next/cache';
import { redirect } from 'next/navigation';
import { createClient } from "@/app/lib/supabase/server";

export type CreateState = {
    message?: string | null;
    errors?: {
        name?: string[];
        distributions?: Partial<Record<Category, string[]>>;
        genotypes?: Partial<Record<Category, string[]>>;
    };
};

export type ChildState =
    | { status: "idle" }                                  // initial
    | { status: "success"; ok: true; message?: string }    // optional
    | ({ status: "error" } & ValidationFailed)             // backend validation
    | ({ status: "error" } & GeneticConstraintViolation);  // backend genetic constraint

export type UpdateSheepState = {
    success?: boolean;
    message?: string | null;
    errors?: { name?: string[] };
};

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

const categories: Category[] = ["SWIM", "FLY", "RUN", "POWER", "STAMINA"];
const grades: Grade[] = ["S", "A", "B", "C", "D", "E"];

export async function logout(formData: FormData, nextPath: string = "/login") {
    const supabase = await createClient();
    await supabase.auth.signOut();
    redirect(nextPath);
}

export async function formDataToSheepCreateDTO(formData: FormData): Promise<SheepCreateDTO> {
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
    const childNameRaw = formData.get("name") as string;
    const childName =
        childNameRaw && childNameRaw.trim() !== ""
            ? childNameRaw.trim()
            : null;

    return {
        name: childName,
        genotypes: genotypes,
        distributions: distributions,
        parentRelationshipId: parentRelationshipId,
    };
}

export async function formDataToSheepUpdateDTO(formData: FormData): Promise<SheepUpdateDTO> {
    const createDTO: SheepCreateDTO = await formDataToSheepCreateDTO(formData);
    return {
        name: createDTO.name,
        genotypes: createDTO.genotypes,
        distributions: createDTO.distributions
    }
}

export async function formDataToChildDTO(formData: FormData): Promise<SheepChildDTO> {
    const createDTO: SheepCreateDTO = await formDataToSheepCreateDTO(formData);
    return {
        name: createDTO.name,
        genotypes: createDTO.genotypes,
        distributions: createDTO.distributions,
        parent1Id: Number(formData.get("parent1Id")),
        parent2Id: Number(formData.get("parent2Id"))
    }
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
        headers: { "Content-Type": "application/json" },
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
        headers: { "Content-Type": "application/json" },
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
    const childNameRaw = formData.get("childName") as string | null;

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
        headers: {"Content-Type": "application/json" },
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
) {
    let updateDTO: SheepUpdateDTO;
    try {
        updateDTO = await formDataToSheepUpdateDTO(formData);
    } catch (err) {
        return err instanceof Error && err.message ? { message: err.message, errors: {} } : { message: "Invalid form data", errors: {} };
    }

    // only required while other fields are ignored for now
    if (!updateDTO.name) {
        return { message: "Name is required", errors: { name: ["Name cannot be empty"] } };
    }

    const res = await fetch(`${API_BASE_URL}/sheep/${sheepId}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updateDTO),
    });

    if (!res.ok) {
        return await parseError(res);
    }

    revalidatePath("/sheep");
    revalidatePath(`/sheep/${sheepId}`);
    // no redirect needed if you’re already on the detail page
    return { success: true };
}


export async function recalculateBeliefs() {
    const res = await fetch(`${API_BASE_URL}/breed/recalculate-beliefs`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
    })

    if (!res.ok) {
        return await parseError(res);
    }

    revalidatePath('/sheep');
    redirect('/sheep');
}


export async function deleteSheep(
    sheepId: number,
    prevState: { success?: boolean, errors?: string[] },
    formData: FormData
) {
    const res = await fetch(`${API_BASE_URL}/sheep/${sheepId}`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
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
        headers: { "Content-Type": "application/json" },
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


