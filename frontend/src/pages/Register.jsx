import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { signup } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await signup(form);
      setSuccess('Registration successful! Redirecting to login...');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div style={wrapperStyle}>
      <form onSubmit={handleSubmit} style={formStyle}>
        <h2 style={{ textAlign: 'center', marginBottom: 20 }}>Register</h2>
        {error && <p style={errorStyle}>{error}</p>}
        {success && <p style={successStyle}>{success}</p>}
        <input name="name" placeholder="Full Name" value={form.name} onChange={handleChange} style={inputStyle} required />
        <input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} style={inputStyle} required />
        <input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} style={inputStyle} required />
        <input name="phone" placeholder="Phone" value={form.phone} onChange={handleChange} style={inputStyle} required />
        <button type="submit" style={submitBtnStyle}>Register</button>
        <p style={{ textAlign: 'center', marginTop: 12, fontSize: 14 }}>
          Already have an account? <Link to="/login">Login</Link>
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
const successStyle = { color: '#28a745', fontSize: 14, marginBottom: 8, textAlign: 'center' };
