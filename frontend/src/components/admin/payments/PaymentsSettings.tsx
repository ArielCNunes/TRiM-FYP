import { useEffect, useState } from "react";
import { stripeConnectApi } from "../../../api/endpoints";
import StatusMessage from "../../shared/StatusMessage";

interface ConnectStatus {
  connected: boolean;
  chargesEnabled: boolean;
  detailsSubmitted: boolean;
}

export default function PaymentsSettings() {
  const [status, setStatus] = useState<ConnectStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchStatus();
  }, []);

  // Check for Stripe redirect query params on mount
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const stripeParam = params.get("stripe");
    if (stripeParam === "refresh") {
      // Link expired, get a fresh one
      handleContinueSetup();
    }
    if (stripeParam === "return") {
      // Returned from onboarding, refresh status
      fetchStatus();
    }
  }, []);

  const fetchStatus = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await stripeConnectApi.getStatus();
      setStatus(response.data);
    } catch {
      setError("Failed to load Stripe account status");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAccount = async () => {
    try {
      setActionLoading(true);
      setError(null);
      const response = await stripeConnectApi.createAccount();
      window.location.href = response.data.url;
    } catch {
      setError("Failed to create Stripe account. Please try again.");
      setActionLoading(false);
    }
  };

  const handleContinueSetup = async () => {
    try {
      setActionLoading(true);
      setError(null);
      const response = await stripeConnectApi.getAccountLink();
      window.location.href = response.data.url;
    } catch {
      setError("Failed to get onboarding link. Please try again.");
      setActionLoading(false);
    }
  };

  const handleOpenDashboard = async () => {
    try {
      const response = await stripeConnectApi.getDashboardLink();
      window.open(response.data.url, "_blank");
    } catch {
      setError("Failed to get dashboard link");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-[var(--border-subtle)] border-t-[var(--accent-text)]" />
      </div>
    );
  }

  // Not connected so show setup prompt
  if (!status?.connected) {
    return (
      <div className="space-y-6">
        <h2 className="text-xl font-bold text-[var(--text-primary)]">
          Payment Settings
        </h2>
        {error && <StatusMessage type="error" message={error} />}
        <div className="rounded-lg border border-[var(--border-subtle)] bg-[var(--bg-secondary)] p-8 text-center">
          <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-2">
            Connect your Stripe account
          </h3>
          <p className="text-[var(--text-muted)] mb-6 max-w-md mx-auto">
            To accept online payments from customers, connect a Stripe account.
            You'll be redirected to Stripe to complete the setup.
          </p>
          <button
            onClick={handleCreateAccount}
            disabled={actionLoading}
            className="rounded-md bg-[var(--accent)] px-6 py-3 font-semibold text-white transition hover:bg-[var(--accent-hover)] disabled:opacity-50"
          >
            {actionLoading ? "Redirecting..." : "Connect Stripe Account"}
          </button>
        </div>
      </div>
    );
  }

  // Connected but onboarding incomplete
  if (!status.chargesEnabled || !status.detailsSubmitted) {
    return (
      <div className="space-y-6">
        <h2 className="text-xl font-bold text-[var(--text-primary)]">
          Payment Settings
        </h2>
        {error && <StatusMessage type="error" message={error} />}
        <div className="rounded-lg border border-[var(--border-subtle)] bg-[var(--bg-secondary)] p-8 text-center">
          <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-yellow-100 text-yellow-600">
            !
          </div>
          <h3 className="text-lg font-semibold text-[var(--text-primary)] mb-2">
            Stripe setup incomplete
          </h3>
          <p className="text-[var(--text-muted)] mb-6 max-w-md mx-auto">
            Your Stripe account has been created but the onboarding process isn't
            finished yet. Complete the setup to start accepting payments.
          </p>
          <button
            onClick={handleContinueSetup}
            disabled={actionLoading}
            className="rounded-md bg-[var(--accent)] px-6 py-3 font-semibold text-white transition hover:bg-[var(--accent-hover)] disabled:opacity-50"
          >
            {actionLoading ? "Redirecting..." : "Continue Setup"}
          </button>
        </div>
      </div>
    );
  }

  // Fully connected and active
  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold text-[var(--text-primary)]">
        Payment Settings
      </h2>
      {error && <StatusMessage type="error" message={error} />}
      <div className="rounded-lg border border-[var(--border-subtle)] bg-[var(--bg-secondary)] p-8">
        <div className="flex items-center gap-3 mb-4">
          <span className="inline-flex h-3 w-3 rounded-full bg-green-500" />
          <h3 className="text-lg font-semibold text-[var(--text-primary)]">
            Stripe connected
          </h3>
        </div>
        <p className="text-[var(--text-muted)] mb-6">
          Your Stripe account is active and ready to accept payments. Manage
          your payouts, refunds, and settings directly from the Stripe
          Dashboard.
        </p>
        <button
          onClick={handleOpenDashboard}
          className="rounded-md border border-[var(--border-subtle)] bg-[var(--bg-primary)] px-5 py-2.5 font-medium text-[var(--text-primary)] transition hover:bg-[var(--bg-secondary)]"
        >
          Open Stripe Dashboard
        </button>
      </div>
    </div>
  );
}
