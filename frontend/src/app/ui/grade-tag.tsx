import { Grade } from '@/app/lib/definitions';

const gradeBackgrounds: Record<Grade, string> = {
    "S": "bg-[#ffffff] bg-clip-text text-transparent",
    "A": "bg-[#b50e0e] bg-clip-text text-transparent",
    "B": "bg-[#de3914] bg-clip-text text-transparent",
    "C": "bg-[#f9ff4f] bg-clip-text text-transparent",
    "D": "bg-[#44db9a] bg-clip-text text-transparent",
    "E": "bg-[#0911ad] bg-clip-text text-transparent",
};

export default function GradeTag({ grade }: { grade: Grade}) {
    return (
        <span className={`font-bold ${gradeBackgrounds[grade]} [text-stroke:1px_white]`}>{grade}</span>
    );
}
