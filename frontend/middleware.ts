import { NextResponse, type NextRequest } from "next/server";
import { createServerClient } from "@supabase/ssr";

export async function middleware(req: NextRequest) {
    let res = NextResponse.next({
        request: {
            headers: req.headers,
        },
    });

    const supabase = createServerClient(
        process.env.NEXT_PUBLIC_SUPABASE_URL!,
        process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY!,
        {
            cookies: {
                getAll: () => req.cookies.getAll(),
                setAll: (cookiesToSet) => {
                    // Start from the existing request cookies
                    const requestCookies = new Map(
                        req.cookies.getAll().map((c) => [c.name, c.value])
                    );

                    // Apply refreshed cookies into the request-side view
                    for (const { name, value } of cookiesToSet) {
                        requestCookies.set(name, value);
                    }

                    // Rebuild the Cookie header with all cookies preserved
                    const requestHeaders = new Headers(req.headers);
                    const cookieHeader = Array.from(requestCookies.entries())
                        .map(([name, value]) => `${name}=${value}`)
                        .join("; ");

                    requestHeaders.set("cookie", cookieHeader);

                    // Replace the downstream request with updated headers
                    res = NextResponse.next({
                        request: {
                            headers: requestHeaders,
                        },
                    });

                    // Also set response cookies so the browser stores them
                    for (const { name, value, options } of cookiesToSet) {
                        res.cookies.set(name, value, options);
                    }
                },
            },
        }
    );

    const {
        data: { user },
    } = await supabase.auth.getUser();

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

        const redirectRes = NextResponse.redirect(url);

        // Preserve any refreshed cookies on redirect
        for (const cookie of res.cookies.getAll()) {
            redirectRes.cookies.set(cookie);
        }

        return redirectRes;
    }

    if (user && isAuthRoute) {
        const url = req.nextUrl.clone();
        url.pathname = "/sheep";

        const redirectRes = NextResponse.redirect(url);

        // Preserve any refreshed cookies on redirect
        for (const cookie of res.cookies.getAll()) {
            redirectRes.cookies.set(cookie);
        }

        return redirectRes;
    }

    return res;
}

export const config = {
    matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};