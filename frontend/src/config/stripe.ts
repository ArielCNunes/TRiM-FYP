import { loadStripe, type Stripe } from "@stripe/stripe-js";

const publishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;

if (!publishableKey) {
  console.error(
    "Stripe publishable key is not configured. Set VITE_STRIPE_PUBLISHABLE_KEY in .env.local"
  );
}

export const stripePromise: Promise<Stripe | null> = loadStripe(
  publishableKey || ""
);
