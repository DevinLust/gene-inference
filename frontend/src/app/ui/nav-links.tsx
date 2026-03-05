'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import clsx from 'clsx';
import { GiSheep, GiBarn } from "react-icons/gi";
import { SiAwslambda } from "react-icons/si";
import { HiOutlineUsers } from "react-icons/hi";
import { HiDocumentText } from "react-icons/hi2";
import LogoutButton from "./logout-button";

// Map of links to display in the side navigation.
// Depending on the size of the application, this would be stored in a database.
const links = [
    { name: 'Sheep List', href: '/sheep', icon: GiBarn },
    {
        name: 'New Sheep',
        href: '/sheep/create',
        icon: GiSheep,
    },
    {
        name: 'Breeding',
        href: '/breed',
        icon: SiAwslambda,
    },
    {
        name: 'Relationships',
        href: '/relationship',
        icon: HiOutlineUsers,
    },
    {
        name: 'Records',
        href: '/birth-record',
        icon: HiDocumentText,
    }
];

export default function NavLinks() {
    const pathname = usePathname();

    return (
        <div className="flex h-full flex-col justify-between">
            {/* top links */}
            <div className="flex flex-col gap-2">
                {links.map((link) => {
                    const LinkIcon = link.icon;
                    return (
                        <Link
                            key={link.name}
                            href={link.href}
                            className={clsx(
                                'flex h-[48px] grow items-center justify-center gap-2 rounded-md bg-gray-800 text-gray-400 p-3 text-sm font-medium hover:bg-gray-700 hover:text-white md:flex-none md:justify-start md:p-2 md:px-3',
                                {
                                    'bg-blue-600 text-white': pathname === link.href,
                                },
                            )}
                        >
                            <LinkIcon className="h-5 w-5 shrink-0" />
                            <p className="hidden md:block">{link.name}</p>
                        </Link>
                    );
                })}
            </div>

            {/* bottom logout */}
            <div className="mt-4">
                <LogoutButton />
            </div>
        </div>
    );
}
