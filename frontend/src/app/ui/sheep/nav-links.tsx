'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import clsx from 'clsx';

// Map of links to display in the side navigation.
// Depending on the size of the application, this would be stored in a database.
const links = [
    { name: 'Sheep List', href: '/sheep', icon: 'HomeIcon' },
    {
        name: 'New Sheep',
        href: '/sheep/create',
        icon: 'DocumentDuplicateIcon',
    },
    {
        name: 'Breeding',
        href: '/sheep/breed',
        icon: 'LambIcon'
    },
];

export default function NavLinks() {
    const pathname = usePathname();
    return (
        <>
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
                        <p className="hidden md:block">{link.name}</p>
                    </Link>
                );
            })}
        </>
    );
}
