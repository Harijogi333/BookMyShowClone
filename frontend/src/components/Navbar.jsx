import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import SearchBar from './SearchBar';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav style={navStyle}>
      <Link to="/" style={logoStyle}>BookMyShow</Link>
      <SearchBar />
      <div style={linkStyle}>
        {user ? (
          <>
            <Link to="/my-bookings" style={navLinkStyle}>My Bookings</Link>
            {user.role === 'ADMIN' && (
              <Link to="/admin" style={navLinkStyle}>Admin Panel</Link>
            )}
            {user.role === 'THEATER_OWNER' && (
              <Link to="/owner" style={navLinkStyle}>My Theater</Link>
            )}
            <span style={{ color: '#fff', marginRight: 12, fontSize: 13 }}>
              {user.email}
              <span style={roleBadge(user.role)}>{user.role}</span>
            </span>
            <button onClick={handleLogout} style={btnStyle}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login" style={navLinkStyle}>Login</Link>
            <Link to="/register" style={navLinkStyle}>Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

const roleBadge = (role) => ({
  display: 'inline-block', marginLeft: 6, padding: '2px 8px', borderRadius: 10,
  fontSize: 11, fontWeight: 'bold',
  background: role === 'ADMIN' ? '#ffc107' : role === 'THEATER_OWNER' ? '#17a2b8' : '#28a745',
  color: '#fff',
});

const navStyle = {
  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  padding: '12px 24px', background: '#e50914', color: '#fff',
};
const logoStyle = { fontSize: 24, fontWeight: 'bold', color: '#fff', textDecoration: 'none' };
const linkStyle = { display: 'flex', alignItems: 'center', gap: 12 };
const navLinkStyle = { color: '#fff', textDecoration: 'none', fontSize: 14 };
const btnStyle = {
  padding: '6px 16px', border: 'none', borderRadius: 4, cursor: 'pointer',
  background: '#fff', color: '#e50914', fontWeight: 'bold',
};
