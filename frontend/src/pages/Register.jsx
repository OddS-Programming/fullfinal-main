import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register as registerRequest } from '../api';

export default function Register({ onLogin }) {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      const data = await registerRequest(username, email, password);
      onLogin(data);
      navigate('/');
    } catch (err) {
      setError(err.message || 'Не удалось зарегистрироваться');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-layout">
      <section className="auth-showcase hero">
        <div className="hero__eyebrow">Регистрация</div>
        <h1 className="hero__title">Создайте пользователя и сразу начните работать с защищённым API.</h1>
        <p className="hero__text">
          После регистрации приложение сразу получает JWT-токен и переводит вас в защищённую часть без дополнительного входа.
        </p>
        <div className="auth-showcase__list">
          <div className="auth-showcase__item">Сообщения Bean Validation от Spring показываются прямо пользователю.</div>
          <div className="auth-showcase__item">Новые пользователи создаются с ролью USER по умолчанию.</div>
          <div className="auth-showcase__item">Тот же сценарий можно проверить через Postman из готовой коллекции.</div>
        </div>
      </section>

      <section className="auth-card">
        <div className="section-kicker">Регистрация</div>
        <h2 className="auth-card__title">Создайте аккаунт</h2>
        <p className="helper-text">Минимальная длина пароля проверяется на backend.</p>

        <form className="form-grid" onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="register-username">Логин</label>
            <input
              id="register-username"
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              minLength={3}
              maxLength={50}
              autoComplete="username"
              required
            />
          </div>

          <div className="field">
            <label htmlFor="register-email">Почта</label>
            <input
              id="register-email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              required
            />
          </div>

          <div className="field">
            <label htmlFor="register-password">Пароль</label>
            <input
              id="register-password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              minLength={6}
              autoComplete="new-password"
              required
            />
          </div>

          {error && <div className="error-banner">{error}</div>}

          <button type="submit" className="btn btn--primary" disabled={isSubmitting}>
            {isSubmitting ? 'Создание аккаунта...' : 'Зарегистрироваться'}
          </button>
        </form>

        <div className="auth-card__footer">
          Уже есть аккаунт? <Link to="/login">Войти</Link>
        </div>
      </section>
    </div>
  );
}
