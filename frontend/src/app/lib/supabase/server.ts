import { cookies } from "next/headers";
import { createServerClient } from "@supabase/ssr";

export async function createClient() {
    const cookieStore = await cookies();

    return createServerClient(
        process.env.NEXT_PUBLIC_SUPABASE_URL!,
        process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY!,
        {
            cookies: {
                getAll() {
                    return cookieStore.getAll();
                },
                // IMPORTANT: Server Components cannot set cookies.
                setAll() {
                    // no-op; middleware handles refresh/rotation
                },
            },
        }
    );
}
