import { useState, useEffect } from 'react';
import api from '../services/api';

function FormField({ label, name, value, onChange, type = 'text', required = false }) {
  return (
    <div style={{ marginBottom: 10 }}>
      <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>{label}</label>
      <input type={type} name={name} value={value} onChange={onChange} required={required} style={inputStyle} />
    </div>
  );
}

const inputStyle = { width: '100%', padding: '8px 10px', border: '1px solid #ddd', borderRadius: 4, fontSize: 13, boxSizing: 'border-box' };
const btnStyle = { padding: '6px 16px', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold', fontSize: 13 };

export default function AdminDashboard() {
  const [tab, setTab] = useState('movies');
  const [movies, setMovies] = useState([]);
  const [cities, setCities] = useState([]);
  const [message, setMessage] = useState('');
  const [movieForm, setMovieForm] = useState({ title: '', description: '', durationInMinutes: '', language: 'ENGLISH', genre: 'ACTION' });
  const [cityForm, setCityForm] = useState({ name: '' });

  useEffect(() => {
    if (tab === 'movies') api.get('/movies').then((r) => setMovies(r.data)).catch(() => {});
    if (tab === 'cities') api.get('/cities/active').then((r) => setCities(r.data)).catch(() => {});
  }, [tab]);

  const handleAddMovie = async (e) => {
    e.preventDefault();
    try {
      await api.post('/movies/add', { ...movieForm, durationInMinutes: Number(movieForm.durationInMinutes) });
      setMessage('Movie added!');
      setMovieForm({ title: '', description: '', durationInMinutes: '', language: 'ENGLISH', genre: 'ACTION' });
      api.get('/movies').then((r) => setMovies(r.data)).catch(() => {});
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
    }
  };

  const handleAddCity = async (e) => {
    e.preventDefault();
    try {
      await api.post('/cities/add', cityForm);
      setMessage('City added!');
      setCityForm({ name: '' });
      api.get('/cities/active').then((r) => setCities(r.data)).catch(() => {});
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
    }
  };

  const tabs = ['movies', 'cities', 'theaters', 'screens', 'shows'];

  return (
    <div style={{ padding: 24 }}>
      <h2>Admin Dashboard</h2>
      <div style={{ display: 'flex', gap: 8, margin: '16px 0', flexWrap: 'wrap' }}>
        {tabs.map((t) => (
          <button key={t} onClick={() => setTab(t)} style={{
            ...btnStyle, background: tab === t ? '#e50914' : '#eee', color: tab === t ? '#fff' : '#333',
          }}>{t.charAt(0).toUpperCase() + t.slice(1)}</button>
        ))}
      </div>

      {message && <p style={{ color: '#28a745', marginBottom: 12 }}>{message}</p>}

      {tab === 'movies' && (
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          <form onSubmit={handleAddMovie} style={{ flex: 1, minWidth: 280, background: '#fff', padding: 16, borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)' }}>
            <h3 style={{ marginBottom: 12 }}>Add Movie</h3>
            <FormField label="Title" name="title" value={movieForm.title} onChange={(e) => setMovieForm({ ...movieForm, title: e.target.value })} required />
            <FormField label="Description" name="description" value={movieForm.description} onChange={(e) => setMovieForm({ ...movieForm, description: e.target.value })} />
            <FormField label="Duration (min)" name="durationInMinutes" type="number" value={movieForm.durationInMinutes} onChange={(e) => setMovieForm({ ...movieForm, durationInMinutes: e.target.value })} required />
            <div style={{ marginBottom: 10 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Language</label>
              <select name="language" value={movieForm.language} onChange={(e) => setMovieForm({ ...movieForm, language: e.target.value })} style={inputStyle}>
                {['ENGLISH','HINDI','TELUGU','TAMIL','KANNADA','MALAYALAM'].map((l) => <option key={l} value={l}>{l}</option>)}
              </select>
            </div>
            <div style={{ marginBottom: 10 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Genre</label>
              <select name="genre" value={movieForm.genre} onChange={(e) => setMovieForm({ ...movieForm, genre: e.target.value })} style={inputStyle}>
                {['ACTION','COMEDY','DRAMA','HORROR','ROMANCE','SCI_FI','THRILLER','ANIMATION'].map((g) => <option key={g} value={g}>{g}</option>)}
              </select>
            </div>
            <button type="submit" style={{ ...btnStyle, background: '#e50914', color: '#fff' }}>Add Movie</button>
          </form>
          <div style={{ flex: 2, minWidth: 300 }}>
            <h3 style={{ marginBottom: 12 }}>All Movies ({movies.length})</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {movies.map((m) => (
                <div key={m.id} style={{ background: '#fff', padding: 12, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div><strong>{m.title}</strong> <span style={{ fontSize: 12, color: '#666' }}>({m.language}, {m.durationInMinutes} min)</span></div>
                  <span style={{ fontSize: 12, color: m.isActive ? '#28a745' : '#dc3545' }}>{m.isActive ? 'Active' : 'Inactive'}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {tab === 'cities' && (
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          <form onSubmit={handleAddCity} style={{ flex: 1, minWidth: 280, background: '#fff', padding: 16, borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)' }}>
            <h3 style={{ marginBottom: 12 }}>Add City</h3>
            <FormField label="City Name" name="name" value={cityForm.name} onChange={(e) => setCityForm({ ...cityForm, name: e.target.value })} required />
            <button type="submit" style={{ ...btnStyle, background: '#e50914', color: '#fff' }}>Add City</button>
          </form>
          <div style={{ flex: 2, minWidth: 300 }}>
            <h3 style={{ marginBottom: 12 }}>All Cities ({cities.length})</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {cities.map((c) => (
                <div key={c.id} style={{ background: '#fff', padding: 12, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}>
                  <strong>{c.name}</strong>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {(tab === 'theaters' || tab === 'screens' || tab === 'shows') && (
        <p style={{ color: '#666' }}>Use the API directly or extend this dashboard for {tab} management.</p>
      )}
    </div>
  );
}
