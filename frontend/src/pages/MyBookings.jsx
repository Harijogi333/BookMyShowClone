import { useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const PAGE_SIZE = 10;

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  const dt = new Date(dateTimeStr);
  return dt.toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

export default function MyBookings() {
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState({ content: [], totalPages: 0, totalElements: 0, number: 0 });
  const [loading, setLoading] = useState(true);

  const fetchBookings = useCallback(() => {
    setLoading(true);
    api.get('/bookings/my-bookings', { params: { page, size: PAGE_SIZE } })
      .then((res) => setPageData(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page]);

  useEffect(() => { fetchBookings(); }, [fetchBookings]);

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this booking?')) return;
    try {
      await api.post(`/bookings/${id}/cancel`);
      fetchBookings();
    } catch (err) {
      alert(err.response?.data?.message || 'Cancellation failed');
    }
  };

  const { content: bookings, totalPages, number: currentPage } = pageData;

  if (loading) return <div style={centerStyle}>Loading...</div>;

  return (
    <div style={{ padding: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>My Bookings</h2>
        {pageData.totalElements > 0 && (
          <span style={{ fontSize: 14, color: '#666' }}>
            Showing page {currentPage + 1} of {totalPages} ({pageData.totalElements} total)
          </span>
        )}
      </div>

      {bookings.length === 0 ? (
        <p>No bookings yet.</p>
      ) : (
        <>
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

          {totalPages > 1 && (
            <div style={paginationStyle}>
              <button
                onClick={() => setPage((p) => p - 1)}
                disabled={currentPage === 0}
                style={pageBtnStyle(currentPage === 0)}
              >
                Previous
              </button>
              {Array.from({ length: totalPages }, (_, i) => (
                <button
                  key={i}
                  onClick={() => setPage(i)}
                  style={pageNumStyle(i === currentPage)}
                >
                  {i + 1}
                </button>
              ))}
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={currentPage >= totalPages - 1}
                style={pageBtnStyle(currentPage >= totalPages - 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
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
const paginationStyle = {
  display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 6, marginTop: 24,
};
const pageBtnStyle = (disabled) => ({
  padding: '6px 14px', border: '1px solid #ccc', borderRadius: 4, background: disabled ? '#eee' : '#fff',
  color: disabled ? '#999' : '#333', cursor: disabled ? 'not-allowed' : 'pointer', fontSize: 14,
});
const pageNumStyle = (active) => ({
  padding: '6px 12px', border: '1px solid #ccc', borderRadius: 4,
  background: active ? '#e50914' : '#fff', color: active ? '#fff' : '#333',
  fontWeight: active ? 'bold' : 'normal', cursor: 'pointer', fontSize: 14,
});
