"use client";

import { logout } from "@/app/lib/actions";
import { HiArrowRightOnRectangle } from "react-icons/hi2";

export default function LogoutButton() {
    return (
        <form action={logout}>
            <button
                type="submit"
                className="flex h-[48px] w-full items-center justify-center gap-2 rounded-md bg-gray-800 text-gray-400 p-3 text-sm font-medium hover:bg-gray-700 hover:text-white md:justify-start md:p-2 md:px-3"
            >
                <HiArrowRightOnRectangle className="h-5 w-5 shrink-0" />
                <span className="hidden md:block">Logout</span>
            </button>
        </form>
    );
}