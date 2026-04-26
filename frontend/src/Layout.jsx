import { Link, NavLink, Outlet } from 'react-router-dom';

export default function Layout({ user, onLogout }) {
  const isAdmin = user?.role === 'ADMIN';

  return (
    <div className="shell">
      <div className="shell__aurora shell__aurora--left" />
      <div className="shell__aurora shell__aurora--right" />

      <header className="topbar">
        <div className="topbar__inner">
          <Link to="/" className="brand">
            <span className="brand__badge">DP</span>
            <div>
              <div className="brand__title">DevOps Panel</div>
              <div className="brand__subtitle">Проекты, окружения и деплои</div>
            </div>
          </Link>

          <nav className="topbar__nav">
            {user ? (
              <>
                <NavLink to="/" className="navlink">
                  Главная
                </NavLink>
                <NavLink to="/projects/new" className="navlink">
                  Новый проект
                </NavLink>
                <div className="userchip">
                  <span className="userchip__name">{user.username}</span>
                  <span className={`userchip__role ${isAdmin ? 'userchip__role--admin' : ''}`}>
                    {isAdmin ? 'ADMIN' : user.role}
                  </span>
                </div>
                <button type="button" className="btn btn--ghost" onClick={onLogout}>
                  Выйти
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login" className="navlink">
                  Вход
                </NavLink>
                <NavLink to="/register" className="navlink">
                  Регистрация
                </NavLink>
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="page">
        <Outlet />
      </main>
    </div>
  );
}
