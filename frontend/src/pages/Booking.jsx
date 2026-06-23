import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import SeatGrid from '../components/SeatGrid';
import PaymentModal from '../components/PaymentModal';

export default function Booking() {
  const { showId } = useParams();
  const navigate = useNavigate();
  const [show, setShow] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [createdBooking, setCreatedBooking] = useState(null);
  const [showPayment, setShowPayment] = useState(false);

  useEffect(() => {
    api.get(`/shows/${showId}`).then((res) => setShow(res.data)).catch(() => {});
  }, [showId]);

  const handleSeatSelect = (seat) => {
    if (createdBooking) return;
    setSelectedSeats((prev) => {
      const exists = prev.find((s) => s.id === seat.id);
      if (exists) return prev.filter((s) => s.id !== seat.id);
      return [...prev, seat];
    });
  };

  const totalPrice = selectedSeats.reduce((sum, s) => sum + (s.price || 0), 0);

  const handleBook = async () => {
    if (selectedSeats.length === 0) return;
    setLoading(true);
    setError('');
    try {
      const res = await api.post('/bookings', {
        showSeatIds: selectedSeats.map((s) => s.id),
      });
      setCreatedBooking(res.data);
      setShowPayment(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Booking failed. Some seats may already be taken.');
    } finally {
      setLoading(false);
    }
  };

  const handlePaymentSuccess = () => {
    setShowPayment(false);
    navigate('/my-bookings');
  };

  const handlePaymentClose = () => {
    setShowPayment(false);
    setCreatedBooking(null);
  };

  if (!show) return <div style={centerStyle}>Loading...</div>;

  return (
    <div style={{ padding: 24 }}>
      <h2>Select Seats</h2>
      {show.movieTitle && <p><strong>{show.movieTitle}</strong> - {show.theaterName}</p>}
      <p>{show.startTime} | {show.screenName}</p>

      <div style={{ marginTop: 24 }}>
        <SeatGrid showId={showId} onSelect={handleSeatSelect} selectedSeatIds={selectedSeats.map((s) => s.id)} />
      </div>

      {selectedSeats.length > 0 && !createdBooking && (
        <div style={summaryStyle}>
          <p><strong>Selected Seats:</strong> {selectedSeats.map((s) => s.seatNumber).join(', ')}</p>
          <p><strong>Total:</strong> ₹{totalPrice}</p>
          {error && <p style={{ color: '#e50914' }}>{error}</p>}
          <button onClick={handleBook} disabled={loading} style={btnStyle}>
            {loading ? 'Booking...' : 'Confirm Booking'}
          </button>
        </div>
      )}

      {showPayment && createdBooking && (
        <PaymentModal
          booking={createdBooking}
          onClose={handlePaymentClose}
          onSuccess={handlePaymentSuccess}
        />
      )}
    </div>
  );
}

const centerStyle = { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' };
const summaryStyle = {
  marginTop: 24, padding: 16, background: '#fff', borderRadius: 8,
  boxShadow: '0 2px 8px rgba(0,0,0,0.1)', maxWidth: 400,
};
const btnStyle = {
  width: '100%', padding: 10, background: '#e50914', color: '#fff', border: 'none',
  borderRadius: 4, fontSize: 16, fontWeight: 'bold', cursor: 'pointer', marginTop: 8,
};
