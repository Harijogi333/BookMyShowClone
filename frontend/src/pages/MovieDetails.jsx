import { useState, useEffect, useMemo } from 'react';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import api from '../services/api';

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function formatTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  const dt = new Date(dateTimeStr);
  return dt.toLocaleString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: true });
}

function toDateStr(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function getNextDays(count) {
  const days = [];
  const today = new Date();
  for (let i = 0; i < count; i++) {
    const d = new Date(today);
    d.setDate(today.getDate() + i);
    days.push({
      dateStr: toDateStr(d),
      dayName: DAY_NAMES[d.getDay()],
      dateNum: d.getDate(),
      month: MONTH_NAMES[d.getMonth()],
      isToday: i === 0,
    });
  }
  return days;
}

export default function MovieDetails() {
  const { movieId } = useParams();
  const [searchParams] = useSearchParams();
  const cityId = searchParams.get('cityId');
  const [movie, setMovie] = useState(null);
  const [theaterMap, setTheaterMap] = useState({});
  const [error, setError] = useState('');
  const [selectedDate, setSelectedDate] = useState(toDateStr(new Date()));

  const dateTabs = useMemo(() => getNextDays(7), []);

  useEffect(() => {
    api.get(`/movies/${movieId}`).then((res) => setMovie(res.data)).catch(() => setError('Failed to load movie details. Is the backend running?'));
  }, [movieId]);

  function isShowOnDate(show, dateStr) {
    if (!show.startTime) return false;
    const showDate = new Date(show.startTime);
    const [y, m, d] = dateStr.split('-').map(Number);
    return showDate.getFullYear() === y && showDate.getMonth() === m - 1 && showDate.getDate() === d;
  }

  useEffect(() => {
    if (cityId) {
      if (!selectedDate) return;
      api.get(`/shows/movie/${movieId}/city/${cityId}/date/${selectedDate}`)
        .then((res) => setTheaterMap(res.data))
        .catch(() => setTheaterMap({}));
    } else {
      api.get(`/shows/movie/${movieId}`)
        .then((res) => {
          const filtered = {};
          Object.entries(res.data).forEach(([theaterId, shows]) => {
            const matching = shows.filter((s) => isShowOnDate(s, selectedDate));
            if (matching.length > 0) {
              filtered[theaterId] = matching;
            }
          });
          setTheaterMap(filtered);
        })
        .catch(() => setTheaterMap({}));
    }
  }, [movieId, cityId, selectedDate]);

  if (error) return <div style={centerStyle}><p>{error}</p><p><a href="/" style={{ color: '#e50914' }}>Go back home</a></p></div>;
  if (!movie) return <div style={centerStyle}>Loading...</div>;

  const theaterEntries = Object.entries(theaterMap);

  return (
    <div style={{ padding: 24 }}>
      <div style={{ display: 'flex', gap: 24, marginBottom: 32 }}>
        <div style={posterWrap}>{movie.imageUrl ? <img src={movie.imageUrl} alt={movie.title} style={imgStyle} /> : <div style={posterText}>{movie.title[0]}</div>}</div>
        <div>
          <h1>{movie.title}</h1>
          <p><strong>Genre:</strong> {movie.genre}</p>
          <p><strong>Language:</strong> {movie.language}</p>
          <p><strong>Duration:</strong> {movie.durationInMinutes} min</p>
          {movie.description && <p>{movie.description}</p>}
        </div>
      </div>

      <h2 style={{ marginBottom: 12 }}>Available Shows</h2>

      <div style={dateTabRowStyle}>
        {dateTabs.map((d) => (
          <button
            key={d.dateStr}
            onClick={() => setSelectedDate(d.dateStr)}
            style={{
              ...dateTabStyle,
              ...(selectedDate === d.dateStr ? dateTabActiveStyle : {}),
            }}
          >
            <span style={dateTabDayStyle}>{d.dayName}</span>
            <span style={dateTabNumStyle}>{d.dateNum}</span>
            <span style={dateTabMonthStyle}>{d.month}</span>
          </button>
        ))}
      </div>

      {theaterEntries.length === 0 ? (
        <p>No shows available.</p>
      ) : (
        theaterEntries.map(([theaterId, theaterShows]) => (
          <div key={theaterId} style={{ marginBottom: 24 }}>
            <h3 style={{ marginBottom: 8 }}>{theaterShows[0]?.theaterName || `Theater #${theaterId}`}</h3>
            <div style={gridStyle}>
              {theaterShows.map((show) => (
                <Link to={`/booking/${show.id}`} key={show.id} style={cardStyle}>
                  <strong>{formatTime(show.startTime)}</strong>
                  <p style={{ fontSize: 13, color: '#666' }}>{show.screenName}</p>
                </Link>
              ))}
            </div>
          </div>
        ))
      )}
    </div>
  );
}

const centerStyle = { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' };
const posterWrap = { width: 200, aspectRatio: '2/3', background: '#e50914', borderRadius: 8, overflow: 'hidden', flexShrink: 0 };
const posterText = { width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 64, color: '#fff', fontWeight: 'bold' };
const imgStyle = { width: '100%', height: '100%', objectFit: 'cover', display: 'block' };
const gridStyle = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 16, marginTop: 12 };
const cardStyle = {
  textDecoration: 'none', color: 'inherit', background: '#fff', padding: 16,
  borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)', textAlign: 'center',
};
const dateTabRowStyle = {
  display: 'flex', gap: 8, marginBottom: 24, flexWrap: 'wrap',
};
const dateTabStyle = {
  display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2,
  padding: '10px 18px', border: '1px solid #ddd', borderRadius: 8,
  background: '#fff', cursor: 'pointer', fontFamily: 'inherit', fontSize: 13,
  minWidth: 68, transition: 'all 0.15s',
};
const dateTabActiveStyle = {
  background: '#e50914', color: '#fff', borderColor: '#e50914', fontWeight: 600,
};
const dateTabDayStyle = { fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.5px' };
const dateTabNumStyle = { fontSize: 20, fontWeight: 700, lineHeight: 1.2 };
const dateTabMonthStyle = { fontSize: 11 };
