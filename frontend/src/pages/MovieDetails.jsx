import { useState, useEffect } from 'react';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import api from '../services/api';

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  const dt = new Date(dateTimeStr);
  return dt.toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

export default function MovieDetails() {
  const { movieId } = useParams();
  const [searchParams] = useSearchParams();
  const cityId = searchParams.get('cityId');
  const [movie, setMovie] = useState(null);
  const [shows, setShows] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get(`/movies/${movieId}`).then((res) => setMovie(res.data)).catch(() => setError('Failed to load movie details. Is the backend running?'));
  }, [movieId]);

  useEffect(() => {
    const url = cityId
      ? `/shows/movie/${movieId}/city/${cityId}`
      : `/shows/movie/${movieId}`;
    api.get(url).then((res) => setShows(res.data)).catch(() => setShows([]));
  }, [movieId, cityId]);

  if (error) return <div style={centerStyle}><p>{error}</p><p><a href="/" style={{ color: '#e50914' }}>Go back home</a></p></div>;
  if (!movie) return <div style={centerStyle}>Loading...</div>;

  return (
    <div style={{ padding: 24 }}>
      <div style={{ display: 'flex', gap: 24, marginBottom: 32 }}>
        <div style={imgPlaceholder}>{movie.title[0]}</div>
        <div>
          <h1>{movie.title}</h1>
          <p><strong>Genre:</strong> {movie.genre}</p>
          <p><strong>Language:</strong> {movie.language}</p>
          <p><strong>Duration:</strong> {movie.durationInMinutes} min</p>
          {movie.description && <p>{movie.description}</p>}
        </div>
      </div>

      <h2>Available Shows</h2>
      {shows.length === 0 ? (
        <p>No shows available.</p>
      ) : (
        <div style={gridStyle}>
          {shows.map((show) => (
            <Link to={`/booking/${show.id}`} key={show.id} style={cardStyle}>
              <strong>{formatDateTime(show.startTime)}</strong>
              <p style={{ fontSize: 13, color: '#666' }}>{show.theaterName} - {show.screenName}</p>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

const centerStyle = { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' };
const imgPlaceholder = {
  width: 200, height: 280, background: '#e50914', display: 'flex', alignItems: 'center',
  justifyContent: 'center', fontSize: 64, color: '#fff', fontWeight: 'bold', borderRadius: 8, flexShrink: 0,
};
const gridStyle = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 16, marginTop: 12 };
const cardStyle = {
  textDecoration: 'none', color: 'inherit', background: '#fff', padding: 16,
  borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)', textAlign: 'center',
};
