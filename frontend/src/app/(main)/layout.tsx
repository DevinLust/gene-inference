import SideNav from "@/app/ui/sidenav";
import { redirect } from "next/navigation";
import { createClient } from "@/app/lib/supabase/server";

export default async function Layout({ children }: { children: React.ReactNode }) {
    const supabase = await createClient();
    const { data: { user } } = await supabase.auth.getUser();

    if (!user) redirect("/login");
    return (
        <div className="flex h-screen flex-col md:flex-row md:overflow-hidden bg-gray-800 text-gray-100">
            <div className="w-full flex-none md:w-64">
                <SideNav />
            </div>
            <div className="flex-grow p-6 md:overflow-y-auto md:p-12">{children}</div>
        </div>
    );
}
