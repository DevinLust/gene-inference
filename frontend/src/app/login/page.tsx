"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { createClient } from "@/app/lib/supabase/browser";

export default function LoginPage() {
    const supabase = createClient();
    const router = useRouter();
    const params = useSearchParams();
    const nextPath = params.get("next") ?? "/";

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        const { error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) {
            alert(error.message);
            return;
        }
        router.replace(nextPath);
    }

    return (
        <form onSubmit={onSubmit} style={{ maxWidth: 420 }}>
            <h1>Login</h1>
            <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="email" />
            <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="password" type="password" />
            <button type="submit">Sign in</button>
        </form>
    );
}