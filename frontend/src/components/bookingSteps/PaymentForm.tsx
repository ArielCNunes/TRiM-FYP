import { useState } from "react";
import type { FormEvent } from "react";
import {
  useStripe,
  useElements,
  PaymentElement,
  ExpressCheckoutElement,
} from "@stripe/react-stripe-js";
import type { StripeExpressCheckoutElementConfirmEvent } from "@stripe/stripe-js";

/**
 * PaymentForm Component
 *
 * Handles Stripe payment for booking deposit.
 * Uses Stripe's PaymentElement for secure card input.
 */
export function PaymentForm({
  depositAmount,
  onPaymentSuccess,
  onPaymentError,
  isProcessing,
  setIsProcessing,
}: {
  clientSecret: string;
  bookingId: number;
  depositAmount: number;
  onPaymentSuccess: (paymentIntentId: string) => void;
  onPaymentError: (error: string) => void;
  isProcessing: boolean;
  setIsProcessing: (processing: boolean) => void;
}) {
  const stripe = useStripe();
  const elements = useElements();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [showExpressCheckout, setShowExpressCheckout] = useState(true);

  const handleExpressCheckoutConfirm = async (_event: StripeExpressCheckoutElementConfirmEvent) => {
    if (!stripe || !elements) {
      return;
    }

    setIsProcessing(true);
    setErrorMessage(null);

    try {
      const { error, paymentIntent } = await stripe.confirmPayment({
        elements,
        confirmParams: {
          return_url: `${window.location.origin}/booking-success`,
        },
        redirect: "if_required",
      });

      if (error) {
        setErrorMessage(error.message || "Payment failed");
        onPaymentError(error.message || "Payment failed");
      } else if (paymentIntent && paymentIntent.status === "succeeded") {
        onPaymentSuccess(paymentIntent.id);
      }
    } catch (err: any) {
      const message = err.message || "An unexpected error occurred";
      setErrorMessage(message);
      onPaymentError(message);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setIsProcessing(true);
    setErrorMessage(null);

    try {
      const { error, paymentIntent } = await stripe.confirmPayment({
        elements,
        confirmParams: {
          return_url: `${window.location.origin}/booking-success`,
        },
        redirect: "if_required",
      });

      if (error) {
        setErrorMessage(error.message || "Payment failed");
        onPaymentError(error.message || "Payment failed");
      } else if (paymentIntent && paymentIntent.status === "succeeded") {
        onPaymentSuccess(paymentIntent.id);
      }
    } catch (err: any) {
      const message = err.message || "An unexpected error occurred";
      setErrorMessage(message);
      onPaymentError(message);
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8 text-white">Payment</h1>

      <div className="border border-zinc-800 rounded-lg p-6 mb-6 bg-zinc-900">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="mb-4">
            <p className="text-zinc-400 mb-2">Amount to pay now:</p>
            <p className="text-3xl font-bold text-indigo-400">
              €{depositAmount.toFixed(2)}
            </p>
          </div>

          {/* Express Checkout for Apple Pay / Google Pay */}
          {showExpressCheckout && (
            <div className="mb-4">
              <ExpressCheckoutElement
                onConfirm={handleExpressCheckoutConfirm}
                onReady={({ availablePaymentMethods }) => {
                  // Hide if no express payment methods available
                  if (!availablePaymentMethods) {
                    setShowExpressCheckout(false);
                  }
                }}
                options={{
                  paymentMethods: {
                    applePay: "always",
                    googlePay: "always",
                    link: "never",
                  },
                }}
              />
              {showExpressCheckout && (
                <div className="flex items-center my-4">
                  <div className="flex-1 border-t border-zinc-700"></div>
                  <span className="px-4 text-zinc-500 text-sm">or pay with card</span>
                  <div className="flex-1 border-t border-zinc-700"></div>
                </div>
              )}
            </div>
          )}

          <div>
            <PaymentElement
              options={{
                layout: "accordion",
                paymentMethodOrder: ["card"],
                wallets: {
                  applePay: "never",
                  googlePay: "never",
                },
              }}
            />
          </div>

          {errorMessage && (
            <div className="p-4 bg-red-900/20 border border-red-800 rounded-md">
              <p className="text-red-400 text-sm">{errorMessage}</p>
            </div>
          )}

          <button
            type="submit"
            disabled={!stripe || isProcessing}
            className="w-full bg-indigo-600 text-white px-6 py-3 rounded-md hover:bg-indigo-500 disabled:bg-zinc-700 font-semibold text-lg transition shadow-lg shadow-indigo-500/20"
          >
            {isProcessing ? (
              <span className="flex items-center justify-center">
                <svg
                  className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Processing Payment...
              </span>
            ) : (
              `Pay €${depositAmount.toFixed(2)}`
            )}
          </button>

          <p className="text-xs text-zinc-500 text-center mt-4">
            Your payment information is securely processed by Stripe.
          </p>
        </form>
      </div>
    </div>
  );
}
