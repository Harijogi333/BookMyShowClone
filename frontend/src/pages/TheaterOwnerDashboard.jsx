import { useState, useEffect } from 'react';
import api from '../services/api';

const inputStyle = { width: '100%', padding: '8px 10px', border: '1px solid #ddd', borderRadius: 4, fontSize: 13, boxSizing: 'border-box' };
const btnStyle = { padding: '6px 16px', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold', fontSize: 13 };

export default function TheaterOwnerDashboard() {
  const [tab, setTab] = useState('screens');
  const [theaters, setTheaters] = useState([]);
  const [screens, setScreens] = useState([]);
  const [shows, setShows] = useState([]);
  const [selectedTheater, setSelectedTheater] = useState('');
  const [selectedScreen, setSelectedScreen] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    api.get('/theaters').then((r) => setTheaters(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (tab === 'screens' && selectedTheater) {
      api.get(`/screens/theater/${selectedTheater}`).then((r) => setScreens(r.data)).catch(() => {});
    }
  }, [tab, selectedTheater]);

  useEffect(() => {
    if (tab === 'shows' && selectedTheater) {
      api.get(`/shows/theater/${selectedTheater}`).then((r) => setShows(r.data)).catch(() => {});
    }
  }, [tab, selectedTheater]);

  const tabs = ['screens', 'shows'];

  return (
    <div style={{ padding: 24 }}>
      <h2>Theater Owner Dashboard</h2>

      <div style={{ margin: '16px 0' }}>
        <label style={{ fontWeight: 'bold', fontSize: 14, display: 'block', marginBottom: 4 }}>Select Theater</label>
        <select value={selectedTheater} onChange={(e) => setSelectedTheater(e.target.value)} style={{ ...inputStyle, width: 300 }}>
          <option value="">-- Choose Theater --</option>
          {theaters.map((t) => <option key={t.id} value={t.id}>{t.name} ({t.cityName})</option>)}
        </select>
      </div>

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
            <div style={{ background: '#fff', padding: 16, borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)' }}>
              <h3 style={{ marginBottom: 12 }}>Screens for Selected Theater</h3>
              {screens.length === 0 ? <p>No screens found.</p> : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {screens.map((s) => (
                    <div key={s.id} style={{ padding: 12, border: '1px solid #eee', borderRadius: 6, display: 'flex', justifyContent: 'space-between' }}>
                      <span><strong>{s.name}</strong></span>
                      <span style={{ fontSize: 13, color: s.isActive ? '#28a745' : '#dc3545' }}>{s.isActive ? 'Active' : 'Inactive'}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {tab === 'shows' && (
            <div style={{ background: '#fff', padding: 16, borderRadius: 8, boxShadow: '0 1px 6px rgba(0,0,0,0.1)' }}>
              <h3 style={{ marginBottom: 12 }}>Shows for Selected Theater</h3>
              {shows.length === 0 ? <p>No shows found.</p> : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {shows.map((s) => (
                    <div key={s.id} style={{ padding: 12, border: '1px solid #eee', borderRadius: 6 }}>
                      <strong>{s.movieTitle}</strong>
                      <p style={{ fontSize: 13, color: '#666' }}>{s.screenName} | {s.startTime}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}
