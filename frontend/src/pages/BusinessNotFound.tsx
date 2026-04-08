import { getBusinessSlug } from "../api/axios";

export default function BusinessNotFound() {
  const slug = getBusinessSlug();

  const handleGoToMain = () => {
    const { protocol, hostname, port } = window.location;
    const parts = hostname.split(".");

    // Strip the subdomain to get back to the main domain
    let baseDomain: string;
    if (parts[parts.length - 1] === "localhost") {
      baseDomain = "localhost";
    } else {
      // e.g., anything.trimbooking.ie → trimbooking.ie
      baseDomain = parts.slice(1).join(".");
    }

    const portSuffix = port ? `:${port}` : "";
    window.location.href = `${protocol}//${baseDomain}${portSuffix}`;
  };

  return (
    <div className="min-h-screen bg-[var(--bg-base)] flex items-center justify-center">
      <div className="text-center max-w-md px-6">
        <h1 className="text-6xl font-bold text-[var(--text-primary)] mb-4">404</h1>
        <h2 className="text-2xl font-semibold text-[var(--text-secondary)] mb-4">
          Business Not Found
        </h2>
        <p className="text-[var(--text-muted)] mb-8">
          No business called <span className="font-mono text-[var(--text-secondary)]">{slug}</span> exists.
        </p>
        <button
          onClick={handleGoToMain}
          className="bg-[var(--accent)] text-white px-8 py-3 rounded-lg font-semibold hover:bg-[var(--accent-hover)] transition shadow-lg shadow-[var(--accent-shadow)]"
        >
          Go to TRiM Homepage
        </button>
      </div>
    </div>
  );
}
