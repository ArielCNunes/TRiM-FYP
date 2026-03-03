import { loadStripe, type Stripe } from "@stripe/stripe-js";

const stripeCache: Record<string, Promise<Stripe | null>> = {};

export function getStripePromise(
  stripeAccount?: string,
): Promise<Stripe | null> {
  const key = stripeAccount || "__platform__";
  if (!stripeCache[key]) {
    stripeCache[key] = loadStripe(
      import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY || "",
      stripeAccount ? { stripeAccount } : undefined,
    );
  }
  return stripeCache[key];
}
