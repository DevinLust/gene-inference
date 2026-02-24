import { HiOutlineEmojiSad } from "react-icons/hi";

export default function SheepNotFound() {
  return (
    <div className="flex flex-col items-center justify-center h-full gap-4 p-6">
      <h2 className="text-2xl font-bold text-gray-500">Resource Not Found</h2>
      <HiOutlineEmojiSad size={256} />
      <p className="text-gray-300 text-sm">
        {"Sorry, we couldn\'t find the resource you\'re looking for."}
      </p>
    </div>
  );
}
