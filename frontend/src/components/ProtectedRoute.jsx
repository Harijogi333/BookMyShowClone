import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div style={centerStyle}>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

const centerStyle = { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' };
