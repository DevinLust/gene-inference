"use client";

import { useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { createClient } from "@/app/lib/supabase/browser";

export default function LoginPage() {
    const supabase = useMemo(() => createClient(), []);
    const router = useRouter();
    const params = useSearchParams();
    const nextPath = params.get("next") ?? "/sheep";

    const [mode, setMode] = useState<"login" | "signup">("login");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    async function onSubmit(e: React.FormEvent) {
        e.preventDefault();
        setErrorMsg(null);
        setLoading(true);

        try {
            if (mode === "login") {
                const { error } = await supabase.auth.signInWithPassword({ email, password });
                if (error) throw error;
                router.replace(nextPath);
                router.refresh();
                return;
            }

            // signup
            const { error } = await supabase.auth.signUp({ email, password });
            if (error) throw error;

            // If email confirmations are disabled, user is signed in immediately.
            // If enabled, they’ll need to confirm, so we keep them on this page.
            router.replace(nextPath);
            router.refresh();
        } catch (err: unknown) {
            setErrorMsg(err instanceof Error ? err.message : "Something went wrong");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-[calc(100vh-0px)] w-full bg-gray-800 text-gray-100 flex items-center justify-center p-4">
            <div className="w-full max-w-md rounded-2xl bg-gray-900/60 shadow-xl ring-1 ring-gray-800 p-6">
                <div className="mb-6">
                    <h1 className="text-2xl font-semibold">
                        {mode === "login" ? "Welcome back" : "Create your account"}
                    </h1>
                    <p className="mt-1 text-sm text-gray-400">
                        {mode === "login"
                            ? "Sign in to manage your sheep and breeding records."
                            : "Sign up to start tracking sheep, breeding, and birth records."}
                    </p>
                </div>

                <div className="mb-5 grid grid-cols-2 rounded-xl bg-gray-800/60 p-1">
                    <button
                        type="button"
                        onClick={() => setMode("login")}
                        className={[
                            "rounded-lg py-2 text-sm font-medium transition",
                            mode === "login" ? "bg-gray-950 text-white" : "text-gray-300 hover:text-white",
                        ].join(" ")}
                    >
                        Sign in
                    </button>
                    <button
                        type="button"
                        onClick={() => setMode("signup")}
                        className={[
                            "rounded-lg py-2 text-sm font-medium transition",
                            mode === "signup" ? "bg-gray-950 text-white" : "text-gray-300 hover:text-white",
                        ].join(" ")}
                    >
                        Sign up
                    </button>
                </div>

                <form onSubmit={onSubmit} className="space-y-4">
                    <label className="block">
                        <span className="text-sm text-gray-300">Email</span>
                        <input
                            className="mt-1 w-full rounded-xl bg-gray-950/40 ring-1 ring-gray-800 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-600"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="you@example.com"
                            type="email"
                            autoComplete="email"
                            required
                        />
                    </label>

                    <label className="block">
                        <span className="text-sm text-gray-300">Password</span>
                        <input
                            className="mt-1 w-full rounded-xl bg-gray-950/40 ring-1 ring-gray-800 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-600"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            type="password"
                            autoComplete={mode === "login" ? "current-password" : "new-password"}
                            minLength={6}
                            required
                        />
                        <p className="mt-1 text-xs text-gray-500">At least 6 characters.</p>
                    </label>

                    {errorMsg && (
                        <div className="rounded-xl bg-red-950/40 ring-1 ring-red-900 px-3 py-2 text-sm text-red-200">
                            {errorMsg}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full rounded-xl bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-500 disabled:opacity-60 disabled:cursor-not-allowed"
                    >
                        {loading
                            ? "Please wait..."
                            : mode === "login"
                                ? "Sign in"
                                : "Create account"}
                    </button>
                </form>

                <div className="mt-5 text-center text-xs text-gray-500">
                    {mode === "login" ? (
                        <span>
              Don’t have an account?{" "}
                            <button
                                type="button"
                                className="text-blue-400 hover:text-blue-300"
                                onClick={() => setMode("signup")}
                            >
                Sign up
              </button>
            </span>
                    ) : (
                        <span>
              Already have an account?{" "}
                            <button
                                type="button"
                                className="text-blue-400 hover:text-blue-300"
                                onClick={() => setMode("login")}
                            >
                Sign in
              </button>
            </span>
                    )}
                </div>
            </div>
        </div>
    );
}
