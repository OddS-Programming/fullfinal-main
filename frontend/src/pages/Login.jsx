import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../api';

export default function Login({ onLogin }) {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      const data = await login(username, password);
      onLogin(data);
      navigate('/');
    } catch (err) {
      setError(err.message || 'Не удалось войти');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-layout">
      <section className="auth-showcase hero">
        <div className="hero__eyebrow">Безопасный доступ</div>
        <h1 className="hero__title">Управляйте поставкой в одном окне.</h1>
        <p className="hero__text">
          Войдите, чтобы управлять проектами, создавать окружения, отслеживать деплои и проверять JWT-защищённый API из реального клиента.
        </p>
        <div className="auth-showcase__list">
          <div className="auth-showcase__item">JWT-токен хранится локально и прикрепляется ко всем защищённым запросам.</div>
          <div className="auth-showcase__item">Ошибки валидации и backend сразу показываются в интерфейсе.</div>
          <div className="auth-showcase__item">Интерфейс общается со Spring Boot только через REST API.</div>
        </div>
      </section>

      <section className="auth-card">
        <div className="section-kicker">Вход</div>
        <h2 className="auth-card__title">С возвращением</h2>
        <p className="helper-text">Введите логин и пароль, чтобы получить JWT-токен.</p>

        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="username">Логин</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              required
            />
          </div>

          <div className="field">
            <label htmlFor="password">Пароль</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />
          </div>

          {error && <div className="error-banner">{error}</div>}

          <button type="submit" className="btn btn--primary" disabled={isSubmitting}>
            {isSubmitting ? 'Вход...' : 'Войти'}
          </button>
        </form>

        <div className="auth-card__footer">
          Нет аккаунта? <Link to="/register">Зарегистрируйтесь</Link>
        </div>
      </section>
    </div>
  );
}
