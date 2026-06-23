import { useState, useEffect, useRef } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import api from '../services/api';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

function PaymentForm({ bookingId, totalPrice, paymentId, onSuccess, onError }) {
  const stripe = useStripe();
  const elements = useElements();
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stripe || !elements) return;

    setProcessing(true);
    setMessage('');

    const { error } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: window.location.origin + '/booking/' + bookingId,
      },
      redirect: 'if_required',
    });

    if (error) {
      setMessage(error.message || 'Payment failed');
      setProcessing(false);
      onError(error);
      return;
    }

    try {
      const res = await api.post('/payments/complete', { paymentId });
      if (res.data.status === 'SUCCESS') {
        onSuccess(res.data);
      } else {
        setMessage('Payment verification failed');
        onError(new Error('Payment verification failed'));
      }
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to verify payment');
      onError(err);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <PaymentElement />
      {message && <p style={{ color: '#e50914', marginTop: 8, fontSize: 14 }}>{message}</p>}
      <button
        type="submit"
        disabled={!stripe || processing}
        style={{
          width: '100%', padding: 12, marginTop: 16, background: '#e50914', color: '#fff',
          border: 'none', borderRadius: 6, fontSize: 16, fontWeight: 'bold', cursor: processing ? 'not-allowed' : 'pointer',
          opacity: !stripe || processing ? 0.7 : 1,
        }}
      >
        {processing ? 'Processing...' : `Pay ₹${totalPrice}`}
      </button>
    </form>
  );
}

export default function PaymentModal({ booking, onClose, onSuccess }) {
  const [clientSecret, setClientSecret] = useState(null);
  const [paymentId, setPaymentId] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const initiatedRef = useRef(false);

  useEffect(() => {
    if (initiatedRef.current) return;
    initiatedRef.current = true;

    api.post('/payments/initiate', {
      bookingId: booking.bookingId,
      paymentMethod: 'CARD',
      amount: booking.totalPrice,
    })
      .then((res) => {
        setClientSecret(res.data.message);
        setPaymentId(res.data.paymentId);
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to initiate payment');
      })
      .finally(() => setLoading(false));
  }, [booking.bookingId, booking.totalPrice]);

  const handleError = (err) => {
    setError(err.message || 'Payment failed');
  };

  return (
    <div style={overlayStyle} onClick={onClose}>
      <div style={modalStyle} onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h3 style={{ margin: 0 }}>Complete Payment</h3>
          <button onClick={onClose} style={closeBtnStyle}>✕</button>
        </div>

        <div style={summaryStyle}>
          <p style={{ margin: '0 0 4px' }}><strong>{booking.movieTitle}</strong></p>
          <p style={{ margin: '0 0 4px', fontSize: 14, color: '#555' }}>{booking.theaterName} | {booking.screenName}</p>
          <p style={{ margin: '0 0 4px', fontSize: 14, color: '#555' }}>{booking.startTime}</p>
          <p style={{ margin: '0 0 4px', fontSize: 14, color: '#555' }}>Seats: {booking.seatNumbers?.join(', ')}</p>
          <p style={{ margin: 0, fontSize: 18, fontWeight: 'bold', color: '#e50914' }}>₹{booking.totalPrice}</p>
        </div>

        {loading && <p style={{ textAlign: 'center', color: '#666' }}>Loading payment...</p>}

        {error && (
          <div>
            <p style={{ color: '#e50914' }}>{error}</p>
            <button onClick={onClose} style={btnStyle}>Close</button>
          </div>
        )}

        {clientSecret && !error && (
          <Elements stripe={stripePromise} options={{ clientSecret, appearance: { theme: 'stripe' } }}>
            <PaymentForm
              bookingId={booking.bookingId}
              totalPrice={booking.totalPrice}
              paymentId={paymentId}
              onSuccess={onSuccess}
              onError={handleError}
            />
          </Elements>
        )}
      </div>
    </div>
  );
}

const overlayStyle = {
  position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', display: 'flex',
  alignItems: 'center', justifyContent: 'center', zIndex: 1000,
};
const modalStyle = {
  background: '#fff', borderRadius: 12, padding: 28, width: 440, maxWidth: '90vw',
  maxHeight: '90vh', overflowY: 'auto',
  boxShadow: '0 8px 32px rgba(0,0,0,0.3)', position: 'relative',
};
const closeBtnStyle = {
  background: 'none', border: 'none', fontSize: 20, cursor: 'pointer', color: '#666', padding: '4px 8px',
};
const summaryStyle = {
  background: '#f8f8f8', borderRadius: 8, padding: 16, marginBottom: 20,
};
const btnStyle = {
  width: '100%', padding: 10, background: '#e50914', color: '#fff', border: 'none',
  borderRadius: 4, fontSize: 16, fontWeight: 'bold', cursor: 'pointer', marginTop: 8,
};
