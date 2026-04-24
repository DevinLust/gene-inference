import CategoryTag from "./category-tag";
import { Category } from "@/app/lib/definitions";

type CardProps = {
    category: Category,
    children?: React.ReactNode;
}

export default function CategoryCard({ category, children }: CardProps) {
    return (
        <div className="border border-gray-500 rounded-lg bg-blue-900 pb-2">
            <div className="w-full bg-blue-500 pl-4 py-1 rounded-t-lg">
                <CategoryTag category={category} />
            </div>
            <div className="px-2">
                {children}
            </div>
        </div>
    );
};
