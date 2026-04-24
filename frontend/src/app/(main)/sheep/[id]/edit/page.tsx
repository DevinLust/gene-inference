import EditSheepForm from "@/app/ui/sheep/edit-form";
import { fetchSheepById } from "@/app/lib/data";
import Link from "next/link";

export default async function EditSheepPage(props: { params: Promise<{ id: string }> }) {
    const params = await props.params;
    const id = params.id;

    const sheep = await fetchSheepById(id);

    return (
        <div>
            <Link href={`/sheep/${sheep.id}`} className="text-blue-400 mb-8">Back to Details</Link>
            <h1 className="text-4xl font-bold leading-tight text-gray-100 mb-6 mt-4">Edit Sheep</h1>

            <EditSheepForm sheep={sheep} />
        </div>
    );
}
