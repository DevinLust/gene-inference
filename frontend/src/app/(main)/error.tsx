"use client";

export default function ErrorPage({
  error,
  reset,
}: {
  error: Error;
  reset: () => void;
}) {
  return (
    <div className="flex flex-col items-center justify-center h-full gap-4 p-6">
      <h2 className="text-xl font-semibold text-red-500">
        Something went wrong
      </h2>

      <p className="text-gray-300 text-sm">
        {error.message}
      </p>

      <button
        onClick={reset}
        className="px-4 py-2 rounded-md bg-blue-600 hover:bg-blue-500 text-white"
      >
        Try again
      </button>
    </div>
  );
}
