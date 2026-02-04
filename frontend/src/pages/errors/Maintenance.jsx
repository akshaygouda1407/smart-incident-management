import bgImage from "../../assets/landing.png";

export default function Maintenance() {
  return (
    <div
      className="min-h-screen bg-cover bg-center flex items-center justify-center relative"
      style={{ backgroundImage: `url(${bgImage})` }}
    >

      <section className="pt-1">
        <div className="relative z-10 text-center max-w-md px-6 transform -translate-y-55">
          <h1 className="text-4xl font-bold text-gray-900">
            We'll Be Back Soon
          </h1>

          <p className="mt-3 text-gray-600">
            ServicePulse is currently undergoing maintenance.
            Please check back shortly.
          </p>

          <p className="mt-4 text-sm text-gray-500">
            Thank you for your patience
          </p>
        </div>
      </section>
    </div>
  );
}
