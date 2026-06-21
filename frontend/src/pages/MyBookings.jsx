import { useState, useEffect } from 'react';
import api from '../services/api';

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  const dt = new Date(dateTimeStr);
  return dt.toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

export default function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchBookings = () => {
    api.get('/bookings/my-bookings')
      .then((res) => setBookings(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchBookings(); }, []);

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this booking?')) return;
    try {
      await api.post(`/bookings/${id}/cancel`);
      fetchBookings();
    } catch (err) {
      alert(err.response?.data?.message || 'Cancellation failed');
    }
  };

  if (loading) return <div style={centerStyle}>Loading...</div>;

  return (
    <div style={{ padding: 24 }}>
      <h2>My Bookings</h2>
      {bookings.length === 0 ? (
        <p>No bookings yet.</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginTop: 16 }}>
          {bookings.map((b) => (
            <div key={b.bookingId} style={cardStyle}>
              <div>
                <strong>{b.movieTitle || 'Movie'}</strong>
                <p style={{ fontSize: 13, color: '#666' }}>{b.theaterName} | {b.screenName}</p>
                <p style={{ fontSize: 13, color: '#666' }}>{formatDateTime(b.startTime)}</p>
                <p style={{ fontSize: 13 }}>Seats: {b.seatNumbers?.join(', ') || ''}</p>
                <p style={{ fontSize: 13 }}>Total: ₹{b.totalPrice}</p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span style={statusBadge(b.status)}>{b.status}</span>
                {b.status === 'CONFIRMED' && (
                  <button onClick={() => handleCancel(b.bookingId)} style={cancelBtnStyle}>Cancel</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

const centerStyle = { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' };
const cardStyle = {
  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  padding: 16, background: '#fff', borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)',
};
const statusBadge = (status) => ({
  display: 'inline-block', padding: '4px 10px', borderRadius: 12, fontSize: 12, fontWeight: 'bold',
  background: status === 'CONFIRMED' ? '#d4edda' : status === 'PENDING' ? '#fff3cd' : status === 'CANCELLED' ? '#f8d7da' : '#e2e3e5',
  color: status === 'CONFIRMED' ? '#155724' : status === 'PENDING' ? '#856404' : status === 'CANCELLED' ? '#721c24' : '#383d41',
});
const cancelBtnStyle = {
  display: 'block', marginTop: 8, padding: '4px 12px', background: '#f8d7da',
  color: '#721c24', border: '1px solid #f5c6cb', borderRadius: 4, cursor: 'pointer', fontSize: 12,
};
