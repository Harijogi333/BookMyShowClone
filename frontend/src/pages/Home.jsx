import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

export default function Home() {
  const [movies, setMovies] = useState([]);
  const [cities, setCities] = useState([]);
  const [selectedCity, setSelectedCity] = useState('');

  useEffect(() => {
    api.get('/cities/active').then((res) => setCities(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    api.get('/movies/active').then((res) => setMovies(res.data)).catch(() => {});
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <h1 style={{ marginBottom: 16 }}>Now Showing</h1>

      {cities.length > 0 && (
        <select value={selectedCity} onChange={(e) => setSelectedCity(e.target.value)} style={selectStyle}>
          <option value="">All Cities</option>
          {cities.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      )}

      <div style={gridStyle}>
        {movies.map((movie) => (
          <Link to={`/movies/${movie.id}${selectedCity ? `?cityId=${selectedCity}` : ''}`} key={movie.id} style={cardStyle}>
            <div style={imgPlaceholder}>{movie.title[0]}</div>
            <div style={cardBody}>
              <h3>{movie.title}</h3>
              <p>{movie.genre} | {movie.language}</p>
              <p style={{ fontSize: 12, color: '#666' }}>{movie.durationInMinutes} min</p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

const gridStyle = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 20, marginTop: 20 };
const cardStyle = {
  textDecoration: 'none', color: 'inherit', background: '#fff', borderRadius: 8,
  overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.1)', transition: 'transform 0.2s',
};
const imgPlaceholder = {
  height: 200, background: '#e50914', display: 'flex', alignItems: 'center',
  justifyContent: 'center', fontSize: 48, color: '#fff', fontWeight: 'bold',
};
const cardBody = { padding: 12 };
const selectStyle = { padding: '8px 12px', borderRadius: 4, border: '1px solid #ddd', fontSize: 14 };
