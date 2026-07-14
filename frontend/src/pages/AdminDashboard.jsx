import { useState, useEffect, useRef } from 'react';
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
const cardStyle = { background: '#fff', padding: 16, borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)' };
const emptyStyle = { fontSize: 13, color: '#dc3545', marginTop: 4 };

export default function AdminDashboard() {
  const [tab, setTab] = useState('movies');
  const [movies, setMovies] = useState([]);
  const [cities, setCities] = useState([]);
  const [message, setMessage] = useState(null);
  const msgRef = useRef(null);

  useEffect(() => {
    if (message) {
      msgRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      const timer = setTimeout(() => setMessage(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [message]);
  const [movieForm, setMovieForm] = useState({ title: '', description: '', durationInMinutes: '', language: 'ENGLISH', genre: 'ACTION' });
  const [cityForm, setCityForm] = useState({ name: '' });
  const [editingMovie, setEditingMovie] = useState(null);
  const [movieEditForm, setMovieEditForm] = useState({ title: '', description: '', durationInMinutes: '', language: 'ENGLISH', genre: 'ACTION', isActive: true });
  const [editingCity, setEditingCity] = useState(null);
  const [cityEditForm, setCityEditForm] = useState({ name: '', isActive: true });

  const [theaters, setTheaters] = useState([]);
  const [theaterForm, setTheaterForm] = useState({ name: '', address: '', cityId: '', ownerId: '', isActive: true });
  const [editingTheater, setEditingTheater] = useState(null);

  const [screens, setScreens] = useState([]);
  const [screenForm, setScreenForm] = useState({ name: '', theaterId: '', isActive: true });
  const [editingScreen, setEditingScreen] = useState(null);
  const [screenTheaterId, setScreenTheaterId] = useState('');

  const [shows, setShows] = useState([]);
  const [showForm, setShowForm] = useState({ movieId: '', screenId: '', startTime: '', isActive: true, seatTypePrices: { SILVER: '', GOLD: '', PLATINUM: '', RECLINER: '' } });
  const [editingShow, setEditingShow] = useState(null);
  const [showTheaterId, setShowTheaterId] = useState('');
  const [showScreenId, setShowScreenId] = useState('');

  const [allTheaters, setAllTheaters] = useState([]);
  const [activeCities, setActiveCities] = useState([]);
  const [filter, setFilter] = useState('active');

  useEffect(() => {
    setFilter('active');
    if (tab === 'movies') api.get('/movies').then((r) => setMovies(r.data)).catch(() => {});
    if (tab === 'cities') api.get('/cities').then((r) => setCities(r.data)).catch(() => {});
    if (tab === 'theaters') { api.get('/theaters').then((r) => setAllTheaters(r.data)).catch(() => {}); api.get('/cities/active').then((r) => setActiveCities(r.data)).catch(() => {}); }
    if (tab === 'screens') api.get('/theaters').then((r) => setAllTheaters(r.data)).catch(() => {});
    if (tab === 'shows') { api.get('/movies/active').then((r) => setMovies(r.data)).catch(() => {}); api.get('/theaters').then((r) => setAllTheaters(r.data)).catch(() => {}); }
  }, [tab]);

  useEffect(() => {
    if (screenTheaterId) api.get(`/screens/theater/${screenTheaterId}`).then((r) => setScreens(r.data)).catch(() => setScreens([]));
    else setScreens([]);
  }, [screenTheaterId]);

  useEffect(() => {
    if (showTheaterId) {
      api.get(`/screens/theater/${showTheaterId}`).then((r) => setScreens(r.data)).catch(() => setScreens([]));
    }
  }, [showTheaterId]);

  useEffect(() => {
    if (showScreenId) api.get(`/shows/screen/${showScreenId}`).then((r) => setShows(r.data)).catch(() => setShows([]));
    else setShows([]);
  }, [showScreenId]);

  useEffect(() => { setMessage(null); }, [tab, screenTheaterId, showTheaterId, showScreenId]);

  const filteredByStatus = (items) => {
    if (filter === 'all') return items;
    return items.filter((i) => filter === 'active' ? i.isActive : !i.isActive);
  };

  const filterDropdown = (
    <select value={filter} onChange={(e) => setFilter(e.target.value)} style={{ ...inputStyle, width: 140, marginBottom: 12 }}>
      <option value="all">All</option>
      <option value="active">Active</option>
      <option value="inactive">Inactive</option>
    </select>
  );

  const refreshMovies = () => api.get('/movies').then((r) => setMovies(r.data)).catch(() => {});
  const refreshCities = () => { api.get('/cities').then((r) => setCities(r.data)).catch(() => {}); api.get('/cities/active').then((r) => setActiveCities(r.data)).catch(() => {}); };
  const refreshTheaters = () => api.get('/theaters').then((r) => setAllTheaters(r.data)).catch(() => {});
  const refreshScreens = () => { if (screenTheaterId) api.get(`/screens/theater/${screenTheaterId}`).then((r) => setScreens(r.data)).catch(() => setScreens([])); };
  const refreshShows = () => { if (showScreenId) api.get(`/shows/screen/${showScreenId}`).then((r) => setShows(r.data)).catch(() => setShows([])); };

  const handleAddMovie = async (e) => {
    e.preventDefault();
    try {
      await api.post('/movies/add', { ...movieForm, durationInMinutes: Number(movieForm.durationInMinutes) });
      setMessage({ text: 'Movie added!', type: 'success' });
      setMovieForm({ title: '', description: '', durationInMinutes: '', language: 'ENGLISH', genre: 'ACTION' });
      refreshMovies();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleAddCity = async (e) => {
    e.preventDefault();
    try {
      await api.post('/cities/add', cityForm);
      setMessage({ text: 'City added!', type: 'success' });
      setCityForm({ name: '' });
      refreshCities();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleEditMovie = (m) => {
    setEditingMovie(m);
    setMovieEditForm({ title: m.title, description: m.description || '', durationInMinutes: m.durationInMinutes, language: m.language, genre: m.genre, isActive: m.isActive });
  };

  const handleMovieEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/movies/${editingMovie.id}`, { ...movieEditForm, durationInMinutes: Number(movieEditForm.durationInMinutes), rating: editingMovie.rating });
      setMessage({ text: 'Movie updated!', type: 'success' });
      setEditingMovie(null);
      setMovieEditForm({ title: '', description: '', durationInMinutes: '', language: 'ENGLISH', genre: 'ACTION', isActive: true });
      refreshMovies();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleDeleteMovie = async (id) => {
    if (!window.confirm('Delete this movie?')) return;
    try {
      await api.delete(`/movies/${id}`);
      setMessage({ text: 'Movie deleted!', type: 'success' });
      refreshMovies();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleEditCity = (c) => {
    setEditingCity(c);
    setCityEditForm({ name: c.name, isActive: c.isActive });
  };

  const handleCityEditSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/cities/${editingCity.id}`, cityEditForm);
      setMessage({ text: 'City updated!', type: 'success' });
      setEditingCity(null);
      setCityEditForm({ name: '', isActive: true });
      refreshCities();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleDeleteCity = async (id) => {
    if (!window.confirm('Delete this city?')) return;
    try {
      await api.delete(`/cities/${id}`);
      setMessage({ text: 'City deleted!', type: 'success' });
      refreshCities();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleTheaterSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = { ...theaterForm, cityId: Number(theaterForm.cityId), ownerId: Number(theaterForm.ownerId) };
      if (editingTheater) {
        await api.put(`/theaters/${editingTheater.id}`, data);
        setMessage({ text: 'Theater updated!', type: 'success' });
        setEditingTheater(null);
      } else {
        await api.post('/theaters/add', data);
        setMessage({ text: 'Theater added!', type: 'success' });
      }
      setTheaterForm({ name: '', address: '', cityId: '', ownerId: '', isActive: true });
      refreshTheaters();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleEditTheater = (t) => {
    setEditingTheater(t);
    setTheaterForm({ name: t.name, address: t.address, cityId: t.cityId, ownerId: t.ownerId, isActive: t.isActive });
  };

  const handleDeleteTheater = async (id) => {
    if (!window.confirm('Delete this theater?')) return;
    try {
      await api.delete(`/theaters/${id}`);
      setMessage({ text: 'Theater deleted!', type: 'success' });
      refreshTheaters();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleScreenSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = { ...screenForm, theaterId: Number(screenForm.theaterId) };
      if (editingScreen) {
        await api.put(`/screens/${editingScreen.id}`, data);
        setMessage({ text: 'Screen updated!', type: 'success' });
        setEditingScreen(null);
      } else {
        await api.post('/screens/add', data);
        setMessage({ text: 'Screen added!', type: 'success' });
      }
      setScreenForm({ name: '', theaterId: '', isActive: true });
      refreshScreens();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleEditScreen = (s) => {
    setEditingScreen(s);
    setScreenForm({ name: s.name, theaterId: s.theaterId, isActive: s.isActive });
  };

  const handleDeleteScreen = async (id) => {
    if (!window.confirm('Delete this screen?')) return;
    try {
      await api.delete(`/screens/${id}`);
      setMessage({ text: 'Screen deleted!', type: 'success' });
      refreshScreens();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleShowSubmit = async (e) => {
    e.preventDefault();
    try {
      const prices = {};
      Object.entries(showForm.seatTypePrices).forEach(([k, v]) => { if (v !== '') prices[k] = Number(v); });
      const data = {
        movieId: Number(showForm.movieId),
        screenId: Number(showScreenId),
        startTime: showForm.startTime,
        isActive: showForm.isActive,
        seatTypePrices: prices,
      };
      if (editingShow) {
        await api.put(`/shows/${editingShow.id}`, data);
        setMessage({ text: 'Show updated!', type: 'success' });
        setEditingShow(null);
      } else {
        await api.post('/shows/add', data);
        setMessage({ text: 'Show added!', type: 'success' });
      }
      setShowForm({ movieId: '', screenId: '', startTime: '', isActive: true, seatTypePrices: { SILVER: '', GOLD: '', PLATINUM: '', RECLINER: '' } });
      refreshShows();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const handleEditShow = (s) => {
    setEditingShow(s);
    setShowForm({
      movieId: s.movieId,
      screenId: s.screenId,
      startTime: s.startTime ? s.startTime.slice(0, 16) : '',
      isActive: s.isActive,
      seatTypePrices: { SILVER: '', GOLD: '', PLATINUM: '', RECLINER: '' },
    });
    setShowScreenId(s.screenId);
  };

  const handleDeleteShow = async (id) => {
    if (!window.confirm('Delete this show?')) return;
    try {
      await api.delete(`/shows/${id}`);
      setMessage({ text: 'Show deleted!', type: 'success' });
      refreshShows();
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Failed', type: 'error' });
    }
  };

  const tabs = ['movies', 'cities', 'theaters', 'screens', 'shows'];

  const msgText = message?.text || '';
  const isError = message?.type === 'error';
  const msgColor = isError ? '#dc3545' : '#28a745';
  const msgBg = isError ? '#f8d7da' : '#d4edda';

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

      {message && <p ref={msgRef} style={{ color: msgColor, marginBottom: 12, padding: 8, background: msgBg, borderRadius: 4 }}>{msgText}</p>}

      {tab === 'movies' && (
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          <form onSubmit={editingMovie ? handleMovieEditSubmit : handleAddMovie} style={{ ...cardStyle, flex: 1, minWidth: 280 }}>
            <h3 style={{ marginBottom: 12 }}>{editingMovie ? 'Edit Movie' : 'Add Movie'}</h3>
            <FormField label="Title" name="title" value={editingMovie ? movieEditForm.title : movieForm.title} onChange={(e) => editingMovie ? setMovieEditForm({ ...movieEditForm, title: e.target.value }) : setMovieForm({ ...movieForm, title: e.target.value })} required />
            <FormField label="Description" name="description" value={editingMovie ? movieEditForm.description : movieForm.description} onChange={(e) => editingMovie ? setMovieEditForm({ ...movieEditForm, description: e.target.value }) : setMovieForm({ ...movieForm, description: e.target.value })} />
            <FormField label="Duration (min)" name="durationInMinutes" type="number" value={editingMovie ? movieEditForm.durationInMinutes : movieForm.durationInMinutes} onChange={(e) => editingMovie ? setMovieEditForm({ ...movieEditForm, durationInMinutes: e.target.value }) : setMovieForm({ ...movieForm, durationInMinutes: e.target.value })} required />
            <div style={{ marginBottom: 10 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Language</label>
              <select name="language" value={editingMovie ? movieEditForm.language : movieForm.language} onChange={(e) => editingMovie ? setMovieEditForm({ ...movieEditForm, language: e.target.value }) : setMovieForm({ ...movieForm, language: e.target.value })} style={inputStyle}>
                {['ENGLISH','HINDI','TELUGU','TAMIL','KANNADA','MALAYALAM'].map((l) => <option key={l} value={l}>{l}</option>)}
              </select>
            </div>
            <div style={{ marginBottom: 10 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Genre</label>
              <select name="genre" value={editingMovie ? movieEditForm.genre : movieForm.genre} onChange={(e) => editingMovie ? setMovieEditForm({ ...movieEditForm, genre: e.target.value }) : setMovieForm({ ...movieForm, genre: e.target.value })} style={inputStyle}>
                {['ACTION','COMEDY','DRAMA','HORROR','ROMANCE','SCI_FI','THRILLER','ANIMATION'].map((g) => <option key={g} value={g}>{g}</option>)}
              </select>
            </div>
            {editingMovie && (
              <div style={{ marginBottom: 10 }}>
                <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Active</label>
                <select name="isActive" value={movieEditForm.isActive} onChange={(e) => setMovieEditForm({ ...movieEditForm, isActive: e.target.value === 'true' })} style={inputStyle}>
                  <option value="true">Yes</option>
                  <option value="false">No</option>
                </select>
              </div>
            )}
            <div style={{ display: 'flex', gap: 8 }}>
              <button type="submit" disabled={editingMovie ? !movieEditForm.title || !movieEditForm.durationInMinutes : !movieForm.title || !movieForm.durationInMinutes} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: (editingMovie ? !movieEditForm.title || !movieEditForm.durationInMinutes : !movieForm.title || !movieForm.durationInMinutes) ? 0.5 : 1, cursor: (editingMovie ? !movieEditForm.title || !movieEditForm.durationInMinutes : !movieForm.title || !movieForm.durationInMinutes) ? 'not-allowed' : 'pointer' }}>{editingMovie ? 'Update' : 'Add'} Movie</button>
              {editingMovie && <button type="button" onClick={() => { setEditingMovie(null); setMovieEditForm({ title: '', description: '', durationInMinutes: '', language: 'ENGLISH', genre: 'ACTION', isActive: true }); }} style={{ ...btnStyle, background: '#666', color: '#fff' }}>Cancel</button>}
            </div>
          </form>
          <div style={{ flex: 2, minWidth: 300 }}>
            {filterDropdown}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {filteredByStatus(movies).length === 0 && <p style={emptyStyle}>No movies found</p>}
              {filteredByStatus(movies).map((m) => (
                <div key={m.id} style={{ background: '#fff', padding: 12, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div><strong>{m.title}</strong> <span style={{ fontSize: 12, color: '#666' }}>({m.language}, {m.durationInMinutes} min)</span></div>
                  <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                    <span style={{ fontSize: 12, color: m.isActive ? '#28a745' : '#dc3545' }}>{m.isActive ? 'Active' : 'Inactive'}</span>
                    <button onClick={() => handleEditMovie(m)} style={{ ...btnStyle, background: '#007bff', color: '#fff', padding: '4px 10px', fontSize: 12 }}>Edit</button>
                    <button onClick={() => handleDeleteMovie(m.id)} disabled={!m.isActive} style={{ ...btnStyle, background: !m.isActive ? '#ccc' : '#dc3545', color: '#fff', padding: '4px 10px', fontSize: 12, cursor: !m.isActive ? 'not-allowed' : 'pointer' }}>Delete</button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {tab === 'cities' && (
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          {editingCity ? (
            <form onSubmit={handleCityEditSubmit} style={{ ...cardStyle, flex: 1, minWidth: 280 }}>
              <h3 style={{ marginBottom: 12 }}>Edit City</h3>
              <FormField label="City Name" name="name" value={cityEditForm.name} onChange={(e) => setCityEditForm({ ...cityEditForm, name: e.target.value })} required />
              <div style={{ marginBottom: 10 }}>
                <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Active</label>
                <select name="isActive" value={cityEditForm.isActive} onChange={(e) => setCityEditForm({ ...cityEditForm, isActive: e.target.value === 'true' })} style={inputStyle}>
                  <option value="true">Yes</option>
                  <option value="false">No</option>
                </select>
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                <button type="submit" disabled={!cityEditForm.name} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: !cityEditForm.name ? 0.5 : 1, cursor: !cityEditForm.name ? 'not-allowed' : 'pointer' }}>Update City</button>
                <button type="button" onClick={() => { setEditingCity(null); setCityEditForm({ name: '', isActive: true }); }} style={{ ...btnStyle, background: '#666', color: '#fff' }}>Cancel</button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleAddCity} style={{ ...cardStyle, flex: 1, minWidth: 280 }}>
              <h3 style={{ marginBottom: 12 }}>Add City</h3>
              <FormField label="City Name" name="name" value={cityForm.name} onChange={(e) => setCityForm({ ...cityForm, name: e.target.value })} required />
              <button type="submit" disabled={!cityForm.name} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: !cityForm.name ? 0.5 : 1, cursor: !cityForm.name ? 'not-allowed' : 'pointer' }}>Add City</button>
            </form>
          )}
          <div style={{ flex: 2, minWidth: 300 }}>
            {filterDropdown}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {filteredByStatus(cities).length === 0 && <p style={emptyStyle}>No cities found</p>}
              {filteredByStatus(cities).map((c) => (
                <div key={c.id} style={{ background: '#fff', padding: 12, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <strong>{c.name}</strong>
                    <span style={{ fontSize: 12, color: c.isActive !== false ? '#28a745' : '#dc3545', marginLeft: 8 }}>{c.isActive !== false ? 'Active' : 'Inactive'}</span>
                  </div>
                  <div style={{ display: 'flex', gap: 6 }}>
                    <button onClick={() => handleEditCity(c)} style={{ ...btnStyle, background: '#007bff', color: '#fff', padding: '4px 10px', fontSize: 12 }}>Edit</button>
                    <button onClick={() => handleDeleteCity(c.id)} disabled={!c.isActive} style={{ ...btnStyle, background: !c.isActive ? '#ccc' : '#dc3545', color: '#fff', padding: '4px 10px', fontSize: 12, cursor: !c.isActive ? 'not-allowed' : 'pointer' }}>Delete</button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {tab === 'theaters' && (
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          <form onSubmit={handleTheaterSubmit} style={{ ...cardStyle, flex: 1, minWidth: 320 }}>
            <h3 style={{ marginBottom: 12 }}>{editingTheater ? 'Edit Theater' : 'Add Theater'}</h3>
            <FormField label="Name" name="name" value={theaterForm.name} onChange={(e) => setTheaterForm({ ...theaterForm, name: e.target.value })} required />
            <FormField label="Address" name="address" value={theaterForm.address} onChange={(e) => setTheaterForm({ ...theaterForm, address: e.target.value })} required />
            <div style={{ marginBottom: 10 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>City</label>
              <select name="cityId" value={theaterForm.cityId} onChange={(e) => setTheaterForm({ ...theaterForm, cityId: e.target.value })} required style={inputStyle}>
                <option value="">-- Select City --</option>
                {activeCities.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              {activeCities.length === 0 && <p style={emptyStyle}>No active cities available</p>}
            </div>
            <FormField label="Owner User ID" name="ownerId" type="number" value={theaterForm.ownerId} onChange={(e) => setTheaterForm({ ...theaterForm, ownerId: e.target.value })} required />
            <div style={{ marginBottom: 10 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Active</label>
              <select name="isActive" value={theaterForm.isActive} onChange={(e) => setTheaterForm({ ...theaterForm, isActive: e.target.value === 'true' })} style={inputStyle}>
                <option value="true">Yes</option>
                <option value="false">No</option>
              </select>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <button type="submit" disabled={!theaterForm.name || !theaterForm.address || !theaterForm.cityId || !theaterForm.ownerId} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: (!theaterForm.name || !theaterForm.address || !theaterForm.cityId || !theaterForm.ownerId) ? 0.5 : 1, cursor: (!theaterForm.name || !theaterForm.address || !theaterForm.cityId || !theaterForm.ownerId) ? 'not-allowed' : 'pointer' }}>{editingTheater ? 'Update' : 'Add'} Theater</button>
              {editingTheater && <button type="button" onClick={() => { setEditingTheater(null); setTheaterForm({ name: '', address: '', cityId: '', ownerId: '', isActive: true }); }} style={{ ...btnStyle, background: '#666', color: '#fff' }}>Cancel</button>}
            </div>
          </form>
          <div style={{ flex: 2, minWidth: 350 }}>
            {filterDropdown}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {filteredByStatus(allTheaters).length === 0 && <p style={emptyStyle}>No theaters found</p>}
              {filteredByStatus(allTheaters).map((t) => (
                <div key={t.id} style={{ background: '#fff', padding: 12, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div><strong>{t.name}</strong> <span style={{ fontSize: 12, color: '#666' }}>({t.cityName})</span></div>
                    <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                      <span style={{ fontSize: 12, color: t.isActive ? '#28a745' : '#dc3545' }}>{t.isActive ? 'Active' : 'Inactive'}</span>
                      <button onClick={() => handleEditTheater(t)} style={{ ...btnStyle, background: '#007bff', color: '#fff', padding: '4px 10px', fontSize: 12 }}>Edit</button>
                      <button onClick={() => handleDeleteTheater(t.id)} disabled={!t.isActive} style={{ ...btnStyle, background: !t.isActive ? '#ccc' : '#dc3545', color: '#fff', padding: '4px 10px', fontSize: 12, cursor: !t.isActive ? 'not-allowed' : 'pointer' }}>Delete</button>
                    </div>
                  </div>
                  <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>{t.address}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {tab === 'screens' && (
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          <div style={{ ...cardStyle, flex: 1, minWidth: 320 }}>
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Select Theater</label>
              <select value={screenTheaterId} onChange={(e) => { setScreenTheaterId(e.target.value); setScreenForm({ ...screenForm, theaterId: e.target.value }); }} style={inputStyle}>
                <option value="">-- Choose Theater --</option>
                {allTheaters.filter(t => t.isActive).map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
              </select>
              {allTheaters.filter(t => t.isActive).length === 0 && <p style={emptyStyle}>No active theaters available</p>}
            </div>
            {screenTheaterId && (
              <form onSubmit={handleScreenSubmit}>
                <h3 style={{ marginBottom: 12 }}>{editingScreen ? 'Edit Screen' : 'Add Screen'}</h3>
                <FormField label="Screen Name" name="name" value={screenForm.name} onChange={(e) => setScreenForm({ ...screenForm, name: e.target.value })} required />
                <div style={{ marginBottom: 10 }}>
                  <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Active</label>
                  <select name="isActive" value={screenForm.isActive} onChange={(e) => setScreenForm({ ...screenForm, isActive: e.target.value === 'true' })} style={inputStyle}>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button type="submit" disabled={!screenForm.name} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: !screenForm.name ? 0.5 : 1, cursor: !screenForm.name ? 'not-allowed' : 'pointer' }}>{editingScreen ? 'Update' : 'Add'} Screen</button>
                  {editingScreen && <button type="button" onClick={() => { setEditingScreen(null); setScreenForm({ name: '', theaterId: screenTheaterId, isActive: true }); }} style={{ ...btnStyle, background: '#666', color: '#fff' }}>Cancel</button>}
                </div>
              </form>
            )}
          </div>
          <div style={{ flex: 2, minWidth: 350 }}>
            {screenTheaterId && filterDropdown}
            <h3 style={{ marginBottom: 12 }}>Screens {screenTheaterId ? `(${screens.length})` : ''}</h3>
            {!screenTheaterId ? <p style={{ color: '#666' }}>Select a theater above.</p> : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {filteredByStatus(screens).length === 0 && <p style={emptyStyle}>No screens found for this theater</p>}
                {filteredByStatus(screens).map((s) => (
                  <div key={s.id} style={{ background: '#fff', padding: 12, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div><strong>{s.name}</strong></div>
                    <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                      <span style={{ fontSize: 12, color: s.isActive ? '#28a745' : '#dc3545' }}>{s.isActive ? 'Active' : 'Inactive'}</span>
                      <button onClick={() => handleEditScreen(s)} style={{ ...btnStyle, background: '#007bff', color: '#fff', padding: '4px 10px', fontSize: 12 }}>Edit</button>
                      <button onClick={() => handleDeleteScreen(s.id)} disabled={!s.isActive} style={{ ...btnStyle, background: !s.isActive ? '#ccc' : '#dc3545', color: '#fff', padding: '4px 10px', fontSize: 12, cursor: !s.isActive ? 'not-allowed' : 'pointer' }}>Delete</button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {tab === 'shows' && (
        <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
          <div style={{ ...cardStyle, flex: 1, minWidth: 360 }}>
            <div style={{ marginBottom: 12 }}>
              <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Select Theater</label>
              <select value={showTheaterId} onChange={(e) => { setShowTheaterId(e.target.value); setShowScreenId(''); }} style={inputStyle}>
                <option value="">-- Choose Theater --</option>
                {allTheaters.filter(t => t.isActive).map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
              </select>
              {allTheaters.filter(t => t.isActive).length === 0 && <p style={emptyStyle}>No active theaters available</p>}
            </div>
            {showTheaterId && (
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Select Screen</label>
                <select value={showScreenId} onChange={(e) => setShowScreenId(e.target.value)} style={inputStyle}>
                  <option value="">-- Choose Screen --</option>
                  {screens.filter(s => s.isActive).map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
                {screens.filter(s => s.isActive).length === 0 && <p style={emptyStyle}>No screens available for this theater</p>}
              </div>
            )}
            {showScreenId && (
              <form onSubmit={handleShowSubmit}>
                <h3 style={{ marginBottom: 12 }}>{editingShow ? 'Edit Show' : 'Add Show'}</h3>
                <div style={{ marginBottom: 10 }}>
                  <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Movie</label>
                  <select name="movieId" value={showForm.movieId} onChange={(e) => setShowForm({ ...showForm, movieId: e.target.value })} required style={inputStyle}>
                    <option value="">-- Select Movie --</option>
                    {movies.map((m) => <option key={m.id} value={m.id}>{m.title}</option>)}
                  </select>
                  {movies.length === 0 && <p style={emptyStyle}>No active movies available</p>}
                </div>
                <FormField label="Start Time" name="startTime" type="datetime-local" value={showForm.startTime} onChange={(e) => setShowForm({ ...showForm, startTime: e.target.value })} required />
                <div style={{ marginBottom: 10 }}>
                  <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Active</label>
                  <select name="isActive" value={showForm.isActive} onChange={(e) => setShowForm({ ...showForm, isActive: e.target.value === 'true' })} style={inputStyle}>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                </div>
                <h4 style={{ margin: '12px 0 8px', fontSize: 14 }}>Seat Type Prices</h4>
                {['SILVER', 'GOLD', 'PLATINUM', 'RECLINER'].map((st) => (
                  <FormField key={st} label={`${st} Price (₹)`} name={st} type="number" value={showForm.seatTypePrices[st]} onChange={(e) => setShowForm({ ...showForm, seatTypePrices: { ...showForm.seatTypePrices, [st]: e.target.value } })} required />
                ))}
                <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                  <button type="submit" disabled={!showForm.movieId || !showScreenId || !showForm.startTime || !Object.values(showForm.seatTypePrices).some(v => v !== '')} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: (!showForm.movieId || !showScreenId || !showForm.startTime || !Object.values(showForm.seatTypePrices).some(v => v !== '')) ? 0.5 : 1, cursor: (!showForm.movieId || !showScreenId || !showForm.startTime || !Object.values(showForm.seatTypePrices).some(v => v !== '')) ? 'not-allowed' : 'pointer' }}>{editingShow ? 'Update' : 'Add'} Show</button>
                  {editingShow && <button type="button" onClick={() => { setEditingShow(null); setShowForm({ movieId: '', screenId: '', startTime: '', isActive: true, seatTypePrices: { SILVER: '', GOLD: '', PLATINUM: '', RECLINER: '' } }); }} style={{ ...btnStyle, background: '#666', color: '#fff' }}>Cancel</button>}
                </div>
              </form>
            )}
          </div>
          <div style={{ flex: 2, minWidth: 350 }}>
            {showScreenId && filterDropdown}
            <h3 style={{ marginBottom: 12 }}>Shows {showScreenId ? `(${shows.length})` : ''}</h3>
            {!showScreenId ? <p style={{ color: '#666' }}>Select a theater and screen above.</p> : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {filteredByStatus(shows).length === 0 && <p style={emptyStyle}>No shows found for this screen</p>}
                {filteredByStatus(shows).map((s) => (
                  <div key={s.id} style={{ background: '#fff', padding: 12, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div><strong>{s.movieTitle}</strong></div>
                      <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                        <span style={{ fontSize: 12, color: s.isActive ? '#28a745' : '#dc3545' }}>{s.isActive ? 'Active' : 'Inactive'}</span>
                        <button onClick={() => handleEditShow(s)} style={{ ...btnStyle, background: '#007bff', color: '#fff', padding: '4px 10px', fontSize: 12 }}>Edit</button>
                        <button onClick={() => handleDeleteShow(s.id)} disabled={!s.isActive} style={{ ...btnStyle, background: !s.isActive ? '#ccc' : '#dc3545', color: '#fff', padding: '4px 10px', fontSize: 12, cursor: !s.isActive ? 'not-allowed' : 'pointer' }}>Delete</button>
                      </div>
                    </div>
                    <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>{s.screenName} | {s.startTime}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
