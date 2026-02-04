const steps = [
  "User creates an issue",
  "Manager reviews and sets priority",
  "Engineer resolves the issue",
  "SLA tracked automatically",
];

export default function Workflow() {
  return (
    <section
      id="workflow"
      className="relative py-28 px-6 bg-gradient-to-br from-indigo-50 via-white to-blue-50"
    >
      {/* Soft background shapes */}
      <div className="absolute top-20 right-10 w-72 h-72 bg-blue-300/20 rounded-full blur-3xl" />

      <div className="relative max-w-5xl mx-auto text-center">
        <h2 className="text-4xl font-bold text-gray-900">
          How It Works
        </h2>

        <p className="mt-4 text-gray-600 max-w-xl mx-auto">
          A structured and transparent workflow from issue creation to resolution.
        </p>

        <div className="mt-16 grid gap-8 md:grid-cols-4">
          {steps.map((step, index) => (
            <div
              key={step}
              className="relative p-6 rounded-2xl bg-white/90 backdrop-blur border border-gray-200 shadow-md hover:shadow-lg transition"
            >
              <div className="absolute -top-4 -right-4 w-10 h-10 bg-blue-600 text-white rounded-full flex items-center justify-center font-bold">
                {index + 1}
              </div>
              <p className="mt-6 text-gray-700 font-medium">
                {step}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
