import { useState, useEffect } from 'react';
import api from '../services/api';

const inputStyle = { width: '100%', padding: '8px 10px', border: '1px solid #ddd', borderRadius: 4, fontSize: 13, boxSizing: 'border-box' };
const btnStyle = { padding: '6px 16px', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold', fontSize: 13 };
const cardStyle = { background: '#fff', padding: 16, borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)' };

function FormField({ label, name, value, onChange, type = 'text', required = false }) {
  return (
    <div style={{ marginBottom: 10 }}>
      <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>{label}</label>
      <input type={type} name={name} value={value} onChange={onChange} required={required} style={inputStyle} />
    </div>
  );
}

export default function TheaterOwnerDashboard() {
  const [tab, setTab] = useState('screens');
  const [theaters, setTheaters] = useState([]);
  const [screens, setScreens] = useState([]);
  const [shows, setShows] = useState([]);
  const [seats, setSeats] = useState([]);
  const [selectedTheater, setSelectedTheater] = useState('');
  const [message, setMessage] = useState('');

  const [screenForm, setScreenForm] = useState({ name: '', isActive: true });
  const [editingScreen, setEditingScreen] = useState(null);

  const [movies, setMovies] = useState([]);
  const [showForm, setShowForm] = useState({ movieId: '', screenId: '', startTime: '', isActive: true, seatTypePrices: { SILVER: '', GOLD: '', PLATINUM: '', RECLINER: '' } });
  const [editingShow, setEditingShow] = useState(null);

  const [seatForm, setSeatForm] = useState({ seatNumber: '', seatType: 'SILVER', screenId: '', isActive: true });
  const [editingSeat, setEditingSeat] = useState(null);
  const [selectedScreenForSeats, setSelectedScreenForSeats] = useState('');
  const [bulkSeatCount, setBulkSeatCount] = useState('');
  const [filter, setFilter] = useState('all');

  useEffect(() => { setFilter('all'); }, [tab]);

  const filteredByStatus = (items) => {
    if (filter === 'all') return items;
    return items.filter((i) => filter === 'active' ? i.isActive : !i.isActive);
  };

  const filterDropdown = (
    <select value={filter} onChange={(e) => setFilter(e.target.value)} style={{ ...inputStyle, width: 130, marginBottom: 12 }}>
      <option value="all">All</option>
      <option value="active">Active</option>
      <option value="inactive">Inactive</option>
    </select>
  );

  useEffect(() => {
    api.get('/theaters').then((r) => setTheaters(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (selectedTheater) {
      api.get(`/screens/theater/${selectedTheater}`).then((r) => setScreens(r.data)).catch(() => setScreens([]));
      if (tab === 'shows') api.get(`/shows/theater/${selectedTheater}`).then((r) => setShows(r.data)).catch(() => setShows([]));
    } else {
      setScreens([]);
      setShows([]);
    }
  }, [selectedTheater, tab]);

  useEffect(() => {
    api.get('/movies').then((r) => setMovies(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (selectedScreenForSeats) {
      api.get(`/seats/screen/${selectedScreenForSeats}`).then((r) => setSeats(r.data)).catch(() => setSeats([]));
    } else {
      setSeats([]);
    }
  }, [selectedScreenForSeats]);

  const refreshScreens = () => { if (selectedTheater) api.get(`/screens/theater/${selectedTheater}`).then((r) => setScreens(r.data)).catch(() => setScreens([])); };
  const refreshShows = () => { if (selectedTheater) api.get(`/shows/theater/${selectedTheater}`).then((r) => setShows(r.data)).catch(() => setShows([])); };
  const refreshSeats = () => { if (selectedScreenForSeats) api.get(`/seats/screen/${selectedScreenForSeats}`).then((r) => setSeats(r.data)).catch(() => setSeats([])); };

  const handleScreenSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = { ...screenForm, theaterId: Number(selectedTheater) };
      if (editingScreen) {
        await api.put(`/screens/${editingScreen.id}`, data);
        setMessage('Screen updated!');
        setEditingScreen(null);
      } else {
        await api.post('/screens/add', data);
        setMessage('Screen added!');
      }
      setScreenForm({ name: '', isActive: true });
      refreshScreens();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
    }
  };

  const handleEditScreen = (s) => {
    setEditingScreen(s);
    setScreenForm({ name: s.name, isActive: s.isActive });
  };

  const handleDeleteScreen = async (id) => {
    if (!window.confirm('Delete this screen?')) return;
    try {
      await api.delete(`/screens/${id}`);
      setMessage('Screen deleted!');
      refreshScreens();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
    }
  };

  const handleShowSubmit = async (e) => {
    e.preventDefault();
    try {
      const prices = {};
      Object.entries(showForm.seatTypePrices).forEach(([k, v]) => { if (v !== '') prices[k] = Number(v); });
      const data = {
        movieId: Number(showForm.movieId),
        screenId: Number(showForm.screenId),
        startTime: showForm.startTime,
        isActive: showForm.isActive,
        seatTypePrices: prices,
      };
      if (editingShow) {
        await api.put(`/shows/${editingShow.id}`, data);
        setMessage('Show updated!');
        setEditingShow(null);
      } else {
        await api.post('/shows/add', data);
        setMessage('Show added!');
      }
      setShowForm({ movieId: '', screenId: '', startTime: '', isActive: true, seatTypePrices: { SILVER: '', GOLD: '', PLATINUM: '', RECLINER: '' } });
      refreshShows();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
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
  };

  const handleDeleteShow = async (id) => {
    if (!window.confirm('Delete this show?')) return;
    try {
      await api.delete(`/shows/${id}`);
      setMessage('Show deleted!');
      refreshShows();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
    }
  };

  const handleSeatSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = { ...seatForm, screenId: Number(selectedScreenForSeats) };
      if (editingSeat) {
        await api.put(`/seats/${editingSeat.id}`, data);
        setMessage('Seat updated!');
        setEditingSeat(null);
      } else {
        await api.post('/seats/add', data);
        setMessage('Seat added!');
      }
      setSeatForm({ seatNumber: '', seatType: 'SILVER', screenId: '', isActive: true });
      refreshSeats();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
    }
  };

  const handleBulkAddSeats = async () => {
    if (!bulkSeatCount || !selectedScreenForSeats) return;
    try {
      const count = Number(bulkSeatCount);
      const existing = seats.length;
      const bulkData = [];
      for (let i = 1; i <= count; i++) {
        const seatNum = existing + i;
        const seatType = seatNum <= count * 0.5 ? 'SILVER' : seatNum <= count * 0.8 ? 'GOLD' : seatNum <= count * 0.95 ? 'PLATINUM' : 'RECLINER';
        bulkData.push({ seatNumber: `A${seatNum}`, seatType, screenId: Number(selectedScreenForSeats), isActive: true });
      }
      await api.post('/seats/bulk-add', bulkData);
      setMessage(`${count} seats added!`);
      setBulkSeatCount('');
      refreshSeats();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
    }
  };

  const handleEditSeat = (s) => {
    setEditingSeat(s);
    setSeatForm({ seatNumber: s.seatNumber, seatType: s.seatType, screenId: s.screenId, isActive: s.isActive });
  };

  const handleDeleteSeat = async (id) => {
    if (!window.confirm('Delete this seat?')) return;
    try {
      await api.delete(`/seats/${id}`);
      setMessage('Seat deleted!');
      refreshSeats();
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed');
    }
  };

  const tabs = ['screens', 'shows', 'seats'];

  return (
    <div style={{ padding: 24 }}>
      <h2>Theater Owner Dashboard</h2>

      <div style={{ margin: '16px 0' }}>
        <label style={{ fontWeight: 'bold', fontSize: 14, display: 'block', marginBottom: 4 }}>Select Theater</label>
        <select value={selectedTheater} onChange={(e) => { setSelectedTheater(e.target.value); setSelectedScreenForSeats(''); }} style={{ ...inputStyle, width: 300 }}>
          <option value="">-- Choose Theater --</option>
          {theaters.filter(t => t.isActive).map((t) => <option key={t.id} value={t.id}>{t.name} ({t.cityName})</option>)}
        </select>
      </div>

      {message && <p style={{ color: '#28a745', marginBottom: 12 }}>{message}</p>}

      {selectedTheater && (
        <>
          <div style={{ display: 'flex', gap: 8, margin: '16px 0' }}>
            {tabs.map((t) => (
              <button key={t} onClick={() => setTab(t)} style={{
                ...btnStyle, background: tab === t ? '#e50914' : '#eee', color: tab === t ? '#fff' : '#333',
              }}>{t.charAt(0).toUpperCase() + t.slice(1)}</button>
            ))}
          </div>

          {tab === 'screens' && (
            <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
              <form onSubmit={handleScreenSubmit} style={{ ...cardStyle, flex: 1, minWidth: 300 }}>
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
                  {editingScreen && <button type="button" onClick={() => { setEditingScreen(null); setScreenForm({ name: '', isActive: true }); }} style={{ ...btnStyle, background: '#666', color: '#fff' }}>Cancel</button>}
                </div>
              </form>
              <div style={{ flex: 2, minWidth: 300 }}>
                {filterDropdown}
                <h3 style={{ marginBottom: 12 }}>Screens ({screens.length})</h3>
                {screens.length === 0 ? <p>No screens found.</p> : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {filteredByStatus(screens).map((s) => (
                      <div key={s.id} style={{ background: '#fff', padding: 12, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span><strong>{s.name}</strong></span>
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
              <form onSubmit={handleShowSubmit} style={{ ...cardStyle, flex: 1, minWidth: 360 }}>
                <h3 style={{ marginBottom: 12 }}>{editingShow ? 'Edit Show' : 'Add Show'}</h3>
                <div style={{ marginBottom: 10 }}>
                  <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Screen</label>
                    <select name="screenId" value={showForm.screenId} onChange={(e) => setShowForm({ ...showForm, screenId: e.target.value })} required style={inputStyle}>
                      <option value="">-- Select Screen --</option>
                      {screens.filter(s => s.isActive).map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
                <div style={{ marginBottom: 10 }}>
                  <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Movie</label>
                  <select name="movieId" value={showForm.movieId} onChange={(e) => setShowForm({ ...showForm, movieId: e.target.value })} required style={inputStyle}>
                    <option value="">-- Select Movie --</option>
                    {movies.map((m) => <option key={m.id} value={m.id}>{m.title}</option>)}
                  </select>
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
                  <button type="submit" disabled={!showForm.movieId || !showForm.screenId || !showForm.startTime || !Object.values(showForm.seatTypePrices).some(v => v !== '')} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: (!showForm.movieId || !showForm.screenId || !showForm.startTime || !Object.values(showForm.seatTypePrices).some(v => v !== '')) ? 0.5 : 1, cursor: (!showForm.movieId || !showForm.screenId || !showForm.startTime || !Object.values(showForm.seatTypePrices).some(v => v !== '')) ? 'not-allowed' : 'pointer' }}>{editingShow ? 'Update' : 'Add'} Show</button>
                  {editingShow && <button type="button" onClick={() => { setEditingShow(null); setShowForm({ movieId: '', screenId: '', startTime: '', isActive: true, seatTypePrices: { SILVER: '', GOLD: '', PLATINUM: '', RECLINER: '' } }); }} style={{ ...btnStyle, background: '#666', color: '#fff' }}>Cancel</button>}
                </div>
              </form>
              <div style={{ flex: 2, minWidth: 300 }}>
                {filterDropdown}
                <h3 style={{ marginBottom: 12 }}>Shows ({shows.length})</h3>
                {shows.length === 0 ? <p>No shows found.</p> : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
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

          {tab === 'seats' && (
            <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
              <div style={{ ...cardStyle, flex: 1, minWidth: 320 }}>
                <div style={{ marginBottom: 16 }}>
                  <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Select Screen</label>
                  <select value={selectedScreenForSeats} onChange={(e) => setSelectedScreenForSeats(e.target.value)} style={inputStyle}>
                    <option value="">-- Choose Screen --</option>
                    {screens.filter(s => s.isActive).map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
                {selectedScreenForSeats && (
                  <>
                    <form onSubmit={handleSeatSubmit}>
                      <h3 style={{ marginBottom: 12 }}>{editingSeat ? 'Edit Seat' : 'Add Seat'}</h3>
                      <FormField label="Seat Number" name="seatNumber" value={seatForm.seatNumber} onChange={(e) => setSeatForm({ ...seatForm, seatNumber: e.target.value })} required />
                      <div style={{ marginBottom: 10 }}>
                        <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Seat Type</label>
                        <select name="seatType" value={seatForm.seatType} onChange={(e) => setSeatForm({ ...seatForm, seatType: e.target.value })} style={inputStyle}>
                          {['SILVER', 'GOLD', 'PLATINUM', 'RECLINER'].map((st) => <option key={st} value={st}>{st}</option>)}
                        </select>
                      </div>
                      <div style={{ marginBottom: 10 }}>
                        <label style={{ display: 'block', fontSize: 13, marginBottom: 2, fontWeight: 'bold' }}>Active</label>
                        <select name="isActive" value={seatForm.isActive} onChange={(e) => setSeatForm({ ...seatForm, isActive: e.target.value === 'true' })} style={inputStyle}>
                          <option value="true">Yes</option>
                          <option value="false">No</option>
                        </select>
                      </div>
                      <div style={{ display: 'flex', gap: 8 }}>
                        <button type="submit" disabled={!seatForm.seatNumber} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: !seatForm.seatNumber ? 0.5 : 1, cursor: !seatForm.seatNumber ? 'not-allowed' : 'pointer' }}>{editingSeat ? 'Update' : 'Add'} Seat</button>
                        {editingSeat && <button type="button" onClick={() => { setEditingSeat(null); setSeatForm({ seatNumber: '', seatType: 'SILVER', screenId: '', isActive: true }); }} style={{ ...btnStyle, background: '#666', color: '#fff' }}>Cancel</button>}
                      </div>
                    </form>
                    <hr style={{ margin: '20px 0' }} />
                    <div>
                      <h3 style={{ marginBottom: 12 }}>Bulk Add Seats</h3>
                      <FormField label="Number of seats to add" name="bulkCount" type="number" value={bulkSeatCount} onChange={(e) => setBulkSeatCount(e.target.value)} required />
                      <button type="button" onClick={handleBulkAddSeats} disabled={!bulkSeatCount} style={{ ...btnStyle, background: '#e50914', color: '#fff', opacity: !bulkSeatCount ? 0.5 : 1, cursor: !bulkSeatCount ? 'not-allowed' : 'pointer' }}>Bulk Add</button>
                    </div>
                  </>
                )}
              </div>
              <div style={{ flex: 2, minWidth: 300 }}>
                {selectedScreenForSeats && filterDropdown}
                <h3 style={{ marginBottom: 12 }}>Seats {selectedScreenForSeats ? `(${seats.length})` : ''}</h3>
                {!selectedScreenForSeats ? <p style={{ color: '#666' }}>Select a screen above.</p> : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                    {filteredByStatus(seats).map((s) => (
                      <div key={s.id} style={{ background: '#fff', padding: 10, borderRadius: 6, boxShadow: '0 1px 4px rgba(0,0,0,0.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div><strong>{s.seatNumber}</strong> <span style={{ fontSize: 12, color: '#666' }}>({s.seatType})</span></div>
                        <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                          <span style={{ fontSize: 12, color: s.isActive ? '#28a745' : '#dc3545' }}>{s.isActive ? 'Active' : 'Inactive'}</span>
                          <button onClick={() => handleEditSeat(s)} style={{ ...btnStyle, background: '#007bff', color: '#fff', padding: '4px 10px', fontSize: 12 }}>Edit</button>
                          <button onClick={() => handleDeleteSeat(s.id)} disabled={!s.isActive} style={{ ...btnStyle, background: !s.isActive ? '#ccc' : '#dc3545', color: '#fff', padding: '4px 10px', fontSize: 12, cursor: !s.isActive ? 'not-allowed' : 'pointer' }}>Delete</button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
