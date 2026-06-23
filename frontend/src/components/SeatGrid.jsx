import { useState, useEffect } from 'react';
import api from '../services/api';

function extractRow(seatNumber) {
  return seatNumber ? seatNumber.replace(/[0-9]/g, '') : 'A';
}

function extractNumber(seatNumber) {
  return seatNumber ? seatNumber.replace(/[^0-9]/g, '') : '';
}

export default function SeatGrid({ showId, onSelect, selectedSeatIds }) {
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/shows/${showId}/seats`)
      .then((res) => setSeats(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [showId]);

  if (loading) return <p>Loading seats...</p>;

  const rows = {};
  seats.forEach((seat) => {
    const row = extractRow(seat.seatNumber);
    if (!rows[row]) rows[row] = [];
    rows[row].push(seat);
  });

  const handleSeatClick = (seat) => {
    if (seat.status === 'AVAILABLE' || seat.blockedByCurrentUser) {
      onSelect(seat);
    }
  };

  const getSeatStyle = (seat) => {
    const isSelected = selectedSeatIds.includes(seat.id);
    if (isSelected) return { ...baseSeat, background: '#28a745', color: '#fff', cursor: 'pointer' };
    if (seat.blockedByCurrentUser) return { ...baseSeat, background: '#ffc107', color: '#333', cursor: 'pointer', border: '2px solid #ff9800' };
    if (seat.status === 'BOOKED' || seat.status === 'BLOCKED') return { ...baseSeat, background: '#ddd', color: '#999', cursor: 'not-allowed' };
    if (seat.status === 'CANCELLED') return { ...baseSeat, background: '#fff3cd', color: '#856404', cursor: 'not-allowed' };
    return { ...baseSeat, background: '#fff', border: '2px solid #e50914', cursor: 'pointer' };
  };

  return (
    <div>
      <div style={{ display: 'flex', gap: 16, marginBottom: 16, justifyContent: 'center', flexWrap: 'wrap' }}>
        <span><span style={legendAvailable} /> Available</span>
        <span><span style={legendSelected} /> Selected</span>
        <span><span style={legendPending} /> Your Pending</span>
        <span><span style={legendBooked} /> Booked</span>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
        <div style={screenStyle}>Screen</div>
        {Object.entries(rows).map(([rowName, rowSeats]) => (
          <div key={rowName} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <span style={{ width: 24, fontWeight: 'bold', fontSize: 13 }}>{rowName}</span>
            {rowSeats.map((seat) => (
              <div
                key={seat.id}
                onClick={() => handleSeatClick(seat)}
                style={getSeatStyle(seat)}
                title={`${seat.seatNumber} - ₹${seat.price} (${seat.seatType})`}
              >
                {extractNumber(seat.seatNumber)}
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

const baseSeat = {
  width: 36, height: 36, display: 'flex', alignItems: 'center', justifyContent: 'center',
  fontSize: 12, fontWeight: 'bold', borderRadius: 4, userSelect: 'none',
};
const screenStyle = {
  width: '60%', height: 8, background: '#333', borderRadius: '4px 4px 0 0',
  marginBottom: 16, textAlign: 'center', fontSize: 11, color: '#fff', paddingTop: 2,
};
const legendAvailable = { display: 'inline-block', width: 16, height: 16, background: '#fff', border: '2px solid #e50914', borderRadius: 3, marginRight: 4, verticalAlign: 'middle' };
const legendSelected = { display: 'inline-block', width: 16, height: 16, background: '#28a745', borderRadius: 3, marginRight: 4, verticalAlign: 'middle' };
const legendPending = { display: 'inline-block', width: 16, height: 16, background: '#ffc107', border: '2px solid #ff9800', borderRadius: 3, marginRight: 4, verticalAlign: 'middle' };
const legendBooked = { display: 'inline-block', width: 16, height: 16, background: '#ddd', borderRadius: 3, marginRight: 4, verticalAlign: 'middle' };
