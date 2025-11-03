import { useState } from "react";
import type { FormEvent } from "react";
import {
  useStripe,
  useElements,
  PaymentElement,
} from "@stripe/react-stripe-js";

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
      <h1 className="text-3xl font-bold mb-8">Payment</h1>

      <div className="border border-gray-200 rounded-lg p-6 mb-6">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="mb-4">
            <p className="text-gray-600 mb-2">Amount to pay now:</p>
            <p className="text-3xl font-bold text-blue-600">
              €{depositAmount.toFixed(2)}
            </p>
          </div>

          <div className="border-t border-gray-200 pt-6">
            <PaymentElement
              options={{ layout: "tabs", paymentMethodOrder: ["card"] }}
            />
          </div>

          {errorMessage && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-md">
              <p className="text-red-600 text-sm">{errorMessage}</p>
            </div>
          )}

          <button
            type="submit"
            disabled={!stripe || isProcessing}
            className="w-full bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 disabled:bg-gray-400 font-semibold text-lg transition"
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

          <p className="text-xs text-gray-500 text-center mt-4">
            Your payment information is securely processed by Stripe.
          </p>
        </form>
      </div>
    </div>
  );
}
