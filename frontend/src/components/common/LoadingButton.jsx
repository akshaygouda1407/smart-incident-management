export default function LoadingButton({
  loading,
  text,
  loadingText,
  type = "submit",
}) {
  return (
    <button
      type={type}
      disabled={loading}
      className={`
        w-full
        flex items-center justify-center gap-2
        py-2.5
        rounded-xl
        text-sm font-medium
        transition
        ${loading
          ? "bg-blue-400 cursor-not-allowed"
          : "bg-blue-600 hover:bg-blue-700"}
        text-white
      `}
    >
      {loading && (
        <span className="
          h-4 w-4
          border-2 border-white/60
          border-t-white
          rounded-full
          animate-spin
        " />
      )}

      {loading ? loadingText : text}
    </button>
  );
}
