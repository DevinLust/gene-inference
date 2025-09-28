'use client';

import Form from "@/app/ui/sheep/create-form";
import Link from "next/link";

export default function CreateSheepPage() {
    return (
        <div className="ml-8 mt-8">
            <Link href="/sheep" className="text-blue-400 mb-8">Back to Sheep List</Link>
            <h1 className="text-4xl font-bold leading-tight text-gray-100 mb-6 mt-4">Create Sheep</h1>
            <Form />
        </div>
    );
}
