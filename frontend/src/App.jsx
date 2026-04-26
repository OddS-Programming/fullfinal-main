import { useEffect, useState } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Layout from './Layout';
import { clearAuth, getStoredAuth, storeAuth } from './api';
import Login from './pages/Login';
import ProjectDetail from './pages/ProjectDetail';
import ProjectForm from './pages/ProjectForm';
import ProjectList from './pages/ProjectList';
import Register from './pages/Register';

function toUser(auth) {
  if (!auth.token || !auth.username) {
    return null;
  }

  return {
    token: auth.token,
    username: auth.username,
    role: auth.role || 'USER',
    userId: auth.userId || '',
  };
}

export default function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    setUser(toUser(getStoredAuth()));
  }, []);

  const handleAuthSuccess = (data) => {
    storeAuth(data);
    setUser(toUser(getStoredAuth()));
  };

  const handleLogout = () => {
    clearAuth();
    setUser(null);
  };

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/login"
          element={user ? <Navigate to="/" replace /> : <Login onLogin={handleAuthSuccess} />}
        />
        <Route
          path="/register"
          element={user ? <Navigate to="/" replace /> : <Register onLogin={handleAuthSuccess} />}
        />
        <Route path="/" element={<Layout user={user} onLogout={handleLogout} />}>
          <Route index element={user ? <ProjectList user={user} /> : <Navigate to="/login" replace />} />
          <Route
            path="projects/new"
            element={user ? <ProjectForm /> : <Navigate to="/login" replace />}
          />
          <Route
            path="projects/:id/edit"
            element={user ? <ProjectForm /> : <Navigate to="/login" replace />}
          />
          <Route
            path="projects/:id"
            element={user ? <ProjectDetail /> : <Navigate to="/login" replace />}
          />
        </Route>
        <Route path="*" element={<Navigate to={user ? '/' : '/login'} replace />} />
      </Routes>
    </BrowserRouter>
  );
}
