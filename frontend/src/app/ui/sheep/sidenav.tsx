import Link from 'next/link';
import NavLinks from '@/app/ui/sheep/nav-links';

export default function SideNav() {
    return (
        <div className="flex h-full flex-col px-3 py-4 md:px-2 bg-gray-900">
            <Link
                className="mb-2 flex h-20 items-end justify-start rounded-md bg-blue-600 p-4 md:h-40 text-white"
                href="/"
            >
                <p>Home</p>
            </Link>
            <div className="flex grow flex-row justify-between space-x-2 md:flex-col md:space-x-0 md:space-y-2">
                <NavLinks />
                <div className="hidden h-auto w-full grow rounded-md 'bg-blue-600 text-white' md:block"></div>
            </div>
        </div>
    );
}
