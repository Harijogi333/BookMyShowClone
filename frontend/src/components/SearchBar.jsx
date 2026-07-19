import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

console.log('SearchBar module loaded');

export default function SearchBar() {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const timer = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const doSearch = (value) => {
    console.log('doSearch called with:', value);
    setQuery(value);
    if (timer.current) clearTimeout(timer.current);
    if (!value.trim()) {
      setResults([]);
      setOpen(false);
      return;
    }
    timer.current = setTimeout(async () => {
      try {
        console.log('Calling API for:', value.trim());
        const res = await api.get('/search/' + encodeURIComponent(value.trim()), { params: { limit: 8 } });
        console.log('API response:', res.data);
        setResults(res.data);
        setOpen(true);
      } catch (err) {
        console.error('API error:', err.response || err.message || err);
        setResults([]);
        setOpen(false);
      }
    }, 300);
  };

  const select = (item) => {
    setOpen(false);
    setQuery('');
    setResults([]);
    if (item.type === 'movie') {
      navigate('/movies/' + item.id);
    }
  };

  return (
    <div ref={ref} style={{ position: 'relative', flex: 1, maxWidth: 400, margin: '0 20px' }}>
      <div style={{ display: 'flex', borderRadius: 4, overflow: 'hidden' }}>
        <input
          type="text"
          placeholder="Search movies & theatres..."
          value={query}
          onChange={(e) => { console.log('onChange fired:', e.target.value); doSearch(e.target.value); }}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && results.length > 0) {
              select(results[0]);
            }
          }}
          style={{
            flex: 1, padding: '8px 12px', border: 'none', fontSize: 14, outline: 'none',
          }}
        />
      </div>
      {open && results.length > 0 && (
        <div style={{
          position: 'absolute', top: '100%', left: 0, right: 0, background: '#fff',
          borderRadius: 4, boxShadow: '0 4px 12px rgba(0,0,0,0.15)', zIndex: 100,
          marginTop: 4, maxHeight: 300, overflowY: 'auto',
        }}>
          {results.map((item) => (
            <div key={item.type + '-' + item.id} onClick={() => select(item)} style={{
              padding: '10px 12px', cursor: 'pointer', display: 'flex',
              justifyContent: 'space-between', alignItems: 'center', fontSize: 14,
              borderBottom: '1px solid #f0f0f0', color: '#333',
            }}>
              <span style={{ fontWeight: 500 }}>{item.label}</span>
              <span style={{
                display: 'inline-block', padding: '2px 8px', borderRadius: 10,
                fontSize: 11, fontWeight: 'bold',
                background: item.type === 'movie' ? '#e50914' : '#333',
                color: '#fff',
              }}>{item.type}</span>
            </div>
          ))}
        </div>
      )}
      {open && query.trim() && results.length === 0 && (
        <div style={{
          position: 'absolute', top: '100%', left: 0, right: 0, background: '#fff',
          borderRadius: 4, boxShadow: '0 4px 12px rgba(0,0,0,0.15)', zIndex: 100,
          marginTop: 4,
        }}>
          <div style={{ padding: 12, color: '#999', fontSize: 14 }}>No results found</div>
        </div>
      )}
    </div>
  );
}
