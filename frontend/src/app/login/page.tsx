import { Suspense } from "react";
import LoginPageClient from "./login-page-client";

export default function LoginPage() {
    return (
        <Suspense fallback={<div className="p-4 text-gray-100">Loading...</div>}>
            <LoginPageClient />
        </Suspense>
    );
}
