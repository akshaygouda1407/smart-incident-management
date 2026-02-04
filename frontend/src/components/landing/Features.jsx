const features = [
  {
    title: "Incident Management",
    description:
      "Create, track, and manage incidents across multiple projects with ease.",
  },
  {
    title: "SLA Monitoring",
    description:
      "Automated SLA tracking ensures deadlines are never missed.",
  },
  {
    title: "Role-Based Workflow",
    description:
      "Admins, Managers, Engineers, and Users have clear responsibilities.",
  },
];

export default function Features() {
  return (
    <section
      id="features"
      className="relative py-28 px-6 overflow-hidden bg-gradient-to-br from-blue-850 via-white to-indigo-650"
    >
      {/* Decorative blobs */}
      <div className="absolute -top-24 -left-24 w-96 h-96 bg-blue-200/30 rounded-full blur-3xl" />
      <div className="absolute -bottom-24 -right-24 w-96 h-96 bg-indigo-200/30 rounded-full blur-3xl" />

      <div className="relative max-w-6xl mx-auto text-center">
        <h2 className="text-4xl font-bold text-gray-900">
          Powerful Features
        </h2>

        <p className="mt-4 text-gray-600 max-w-2xl mx-auto">
          Everything you need to manage incidents effectively and at scale.
        </p>

        <div className="mt-16 grid gap-8 md:grid-cols-3">
          {features.map((f) => (
            <div
              key={f.title}
              className="p-8 rounded-2xl bg-white/90 backdrop-blur border border-gray-200 shadow-lg hover:shadow-xl transition"
            >
              <h3 className="text-xl font-semibold text-gray-900">
                {f.title}
              </h3>
              <p className="mt-3 text-gray-600">
                {f.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
