import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    }
  };

  return (
    <div style={wrapperStyle}>
      <form onSubmit={handleSubmit} style={formStyle}>
        <h2 style={{ textAlign: 'center', marginBottom: 20 }}>Login</h2>
        {error && <p style={errorStyle}>{error}</p>}
        <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} style={inputStyle} required />
        <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} style={inputStyle} required />
        <button type="submit" style={submitBtnStyle}>Login</button>
        <p style={{ textAlign: 'center', marginTop: 12, fontSize: 14 }}>
          Don't have an account? <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  );
}

const wrapperStyle = { display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' };
const formStyle = { width: 360, padding: 32, background: '#fff', borderRadius: 8, boxShadow: '0 2px 12px rgba(0,0,0,0.1)' };
const inputStyle = { width: '100%', padding: '10px 12px', marginBottom: 12, border: '1px solid #ddd', borderRadius: 4, fontSize: 14, boxSizing: 'border-box' };
const submitBtnStyle = { width: '100%', padding: 10, background: '#e50914', color: '#fff', border: 'none', borderRadius: 4, fontSize: 16, fontWeight: 'bold', cursor: 'pointer' };
const errorStyle = { color: '#e50914', fontSize: 14, marginBottom: 8, textAlign: 'center' };
