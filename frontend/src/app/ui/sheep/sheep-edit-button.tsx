import Link from "next/link";
import { HiOutlinePencilSquare } from "react-icons/hi2";

export default function EditSheepButton({ sheepId }: { sheepId: number }) {
    return (
        <Link
            href={`/sheep/${sheepId}/edit`}
            className="flex gap-1 h-10 items-center rounded-lg bg-blue-600 px-4 text-sm font-medium text-white transition-colors hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
        >
            <span className="hidden md:block">Edit Sheep</span>
            <HiOutlinePencilSquare />
        </Link>
    );
}