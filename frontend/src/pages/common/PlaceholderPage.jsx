export default function PlaceholderPage({ title, description }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white p-6">
      <h1 className="text-xl font-bold text-gray-800">{title}</h1>
      <p className="mt-2 text-sm text-gray-600">
        {description || "This page is not implemented yet."}
      </p>
    </div>
  );
}

