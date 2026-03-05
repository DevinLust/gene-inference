import { NextResponse, type NextRequest } from "next/server";
import { createServerClient } from "@supabase/ssr";

export async function middleware(req: NextRequest) {
    const res = NextResponse.next();

    const supabase = createServerClient(
        process.env.NEXT_PUBLIC_SUPABASE_URL!,
        process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY!,
        {
            cookies: {
                getAll: () => req.cookies.getAll(),
                setAll: (cookiesToSet) => {
                    cookiesToSet.forEach(({ name, value, options }) => {
                        res.cookies.set(name, value, options);
                    });
                },
            },
        }
    );

    // IMPORTANT: getUser() validates the session and refreshes if needed
    const { data: { user } } = await supabase.auth.getUser();

    const pathname = req.nextUrl.pathname;

    const isAuthRoute = pathname.startsWith("/login");
    const isProtectedRoute =
        pathname.startsWith("/sheep") ||
        pathname.startsWith("/breed") ||
        pathname.startsWith("/relationship") ||
        pathname.startsWith("/birth-record");

    if (!user && isProtectedRoute) {
        const url = req.nextUrl.clone();
        url.pathname = "/login";
        url.searchParams.set("next", pathname);
        return NextResponse.redirect(url);
    }

    if (user && isAuthRoute) {
        const url = req.nextUrl.clone();
        url.pathname = "/sheep";
        return NextResponse.redirect(url);
    }

    return res;
}

export const config = {
    matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
