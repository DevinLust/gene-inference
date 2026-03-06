import { Category } from '@/app/lib/definitions';

const rgbContrast: number = 30;

const categoryColors: Record<Category, string> = {
    "SWIM": "#b8aa11",
    "FLY": "#ac0db8",
    "RUN": "#047a21",
    "POWER": "#a30f1b",
    "STAMINA": "#ab690e",
};

export default function CategoryTag({ category }: { category: Category}) {
    const backgroundColor: string = categoryColors[category];
    const startColor: string = adjustColor(backgroundColor, rgbContrast);
    const endColor: string = adjustColor(backgroundColor, -rgbContrast);
    const categoryText = category.charAt(0).toUpperCase() + category.slice(1).toLowerCase();

    return (
        <span
            className="inline-block items-center px-1 text-white rounded"
            style={{
                background: `radial-gradient(${startColor}, ${backgroundColor} 40%, ${endColor} 100%)`,
            }}
        >
            {categoryText}
        </span>
    );
}

/**
 * Adjusts the brightness of a hex color by adding the amount in terms of rgb
 * to all color components clamped [0, 255].
 *
 * @param hex {string} - The color in hex format
 * @param amount {number} - How much to lighten or darken
 * @return {string} The adjusted color in hex format.
 */
function adjustColor(hex: string, amount: number): string {
    // Remove "#" if present
    const color = hex.startsWith("#") ? hex.slice(1) : hex;

    // Parse each RGB component
    const num = parseInt(color, 16);
    let r = (num >> 16) + amount;
    let g = ((num >> 8) & 0x00ff) + amount;
    let b = (num & 0x0000ff) + amount;

    // Clamp values between 0 and 255
    r = Math.min(255, Math.max(0, r));
    g = Math.min(255, Math.max(0, g));
    b = Math.min(255, Math.max(0, b));

    // Rebuild hex and return with "#"
    return "#" + (b | (g << 8) | (r << 16)).toString(16).padStart(6, "0");
}
