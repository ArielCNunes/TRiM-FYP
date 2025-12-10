import { loadStripe, type Stripe } from "@stripe/stripe-js";

const publishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;

export const stripePromise: Promise<Stripe | null> = loadStripe(
  publishableKey || ""
);
