import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

export default function Home() {
  const [movies, setMovies] = useState([]);
  const [cities, setCities] = useState([]);
  const [selectedCity, setSelectedCity] = useState('');
  const [visibleMovieIds, setVisibleMovieIds] = useState(null);

  useEffect(() => {
    api.get('/cities/active').then((res) => setCities(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    api.get('/movies/active').then((res) => setMovies(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (selectedCity) {
      api.get(`/shows/city/${selectedCity}`).then((res) => {
        const ids = [...new Set(res.data.map((s) => s.movieId))];
        setVisibleMovieIds(ids);
      }).catch(() => setVisibleMovieIds([]));
    } else {
      setVisibleMovieIds(null);
    }
  }, [selectedCity]);

  const filteredMovies = visibleMovieIds ? movies.filter((m) => visibleMovieIds.includes(m.id)) : movies;

  return (
    <div style={{ padding: 24 }}>
      <h1 style={{ marginBottom: 16 }}>Now Showing</h1>

      {cities.length > 0 && (
        <select value={selectedCity} onChange={(e) => setSelectedCity(e.target.value)} style={selectStyle}>
          <option value="">All Cities</option>
          {cities.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      )}

      {selectedCity && filteredMovies.length === 0 && movies.length > 0 && (
        <p style={{ color: '#666', marginTop: 20 }}>No movies currently playing in this city.</p>
      )}

      <div style={gridStyle}>
        {filteredMovies.map((movie) => (
          <Link to={`/movies/${movie.id}?cityId=${selectedCity || ''}`} key={movie.id} style={cardStyle}>
            <div style={posterWrap}>{movie.imageUrl ? <img src={movie.imageUrl} alt={movie.title} style={imgStyle} /> : <div style={posterText}>{movie.title[0]}</div>}</div>
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

const gridStyle = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: 16, marginTop: 20 };
const cardStyle = {
  textDecoration: 'none', color: 'inherit', background: '#fff', borderRadius: 8,
  overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.1)', transition: 'transform 0.2s',
};
const posterWrap = { width: '100%', aspectRatio: '2/3', background: '#e50914', overflow: 'hidden' };
const posterText = { width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 48, color: '#fff', fontWeight: 'bold' };
const imgStyle = { width: '100%', height: '100%', objectFit: 'cover', display: 'block' };
const cardBody = { padding: 12 };
const selectStyle = { padding: '8px 12px', borderRadius: 4, border: '1px solid #ddd', fontSize: 14 };
