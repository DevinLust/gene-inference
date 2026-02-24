'use client';

import BreedForm from "@/app/ui/breed/breed-form";
import Link from "next/link";

export default function CreateBreedPage() {
    return (
        <div className="ml-8 mt-8">
            <Link href="/breed" className="text-blue-400 mb-8">Back to Breeding Dashboard</Link>
            <h1 className="text-4xl font-bold leading-tight text-gray-100 mb-6 mt-4">Breed Sheep</h1>
            <BreedForm />
        </div>
    );
}