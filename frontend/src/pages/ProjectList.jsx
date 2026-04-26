import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getMyProjects, getProjects } from '../api';

function formatDate(value) {
  if (!value) {
    return 'Нет даты';
  }

  return new Date(value).toLocaleString();
}

export default function ProjectList({ user }) {
  const navigate = useNavigate();
  const isAdmin = user?.role === 'ADMIN';
  const [projectsPage, setProjectsPage] = useState({ content: [], number: 0, totalPages: 0, totalElements: 0 });
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [scope, setScope] = useState(isAdmin ? 'all' : 'mine');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let isActive = true;
    setLoading(true);
    setError('');

    const request = scope === 'all' ? getProjects : getMyProjects;

    request({ page, size: 6 })
      .then((data) => {
        if (isActive) {
          setProjectsPage(data);
        }
      })
      .catch((err) => {
        if (isActive) {
          setError(err.message || 'Не удалось загрузить проекты');
        }
      })
      .finally(() => {
        if (isActive) {
          setLoading(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [page, scope]);

  const filteredProjects = useMemo(() => {
    const normalized = search.trim().toLowerCase();
    const list = projectsPage.content || [];

    if (!normalized) {
      return list;
    }

    return list.filter((project) => (
      project.name.toLowerCase().includes(normalized) ||
      (project.description || '').toLowerCase().includes(normalized)
    ));
  }, [projectsPage.content, search]);

  return (
    <div className="stack">
      <section className="hero">
        <div className="hero__grid">
          <div>
            <div className="hero__eyebrow">{isAdmin ? 'Панель администратора' : 'Главная'}</div>
            <h1 className="hero__title">
              {isAdmin ? 'Контролируйте все проекты из одного места.' : 'Создавайте. Деплойте. Отслеживайте.'}
            </h1>
            <p className="hero__text">
              {isAdmin
                ? 'Администратор может просматривать все проекты, быстро находить нужные и управлять ими независимо от владельца.'
                : 'Управляйте своими проектами, держите окружения в порядке и запускайте деплои из одного интерфейса на Spring Boot с JWT-защитой.'}
            </p>
          </div>

          <div className="stack stack--sm">
            <div className="meta-pill">
              {isAdmin ? `Администратор: ${user.username}` : `Вы вошли как ${user.username}`}
            </div>
            <Link to="/projects/new" className="btn btn--primary">
              Создать проект
            </Link>
          </div>
        </div>

        <div className="stats-grid">
          <article className="stat-card">
            <div className="stat-card__label">Проектов на странице</div>
            <div className="stat-card__value">{projectsPage.content?.length || 0}</div>
          </article>
          <article className="stat-card">
            <div className="stat-card__label">
              {scope === 'all' ? 'Всего проектов' : 'Всего ваших проектов'}
            </div>
            <div className="stat-card__value">{projectsPage.totalElements || 0}</div>
          </article>
          <article className="stat-card">
            <div className="stat-card__label">Текущая страница</div>
            <div className="stat-card__value">{(projectsPage.number || 0) + 1}</div>
          </article>
        </div>
      </section>

      <section className="section">
        <div className="section-header">
          <div>
            <div className="section-kicker">Проекты</div>
            <h2 className="section-title">
              {isAdmin ? 'Центр управления проектами' : 'Ваше рабочее пространство'}
            </h2>
          </div>

          <div className="toolbar">
            {isAdmin && (
              <div className="scope-switch" role="tablist" aria-label="Режим просмотра проектов">
                <button
                  type="button"
                  className={`scope-switch__button ${scope === 'all' ? 'scope-switch__button--active' : ''}`}
                  onClick={() => {
                    setPage(0);
                    setScope('all');
                  }}
                >
                  Все проекты
                </button>
                <button
                  type="button"
                  className={`scope-switch__button ${scope === 'mine' ? 'scope-switch__button--active' : ''}`}
                  onClick={() => {
                    setPage(0);
                    setScope('mine');
                  }}
                >
                  Мои проекты
                </button>
              </div>
            )}

            <div className="field toolbar__search">
              <label htmlFor="search-projects">Быстрый фильтр</label>
              <input
                id="search-projects"
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Поиск по загруженным проектам"
              />
            </div>
          </div>
        </div>

        {error && <div className="error-banner">{error}</div>}

        {loading ? (
          <div className="project-grid">
            <div className="skeleton" />
            <div className="skeleton" />
            <div className="skeleton" />
            <div className="skeleton" />
          </div>
        ) : filteredProjects.length === 0 ? (
          <div className="empty-state">
            <h3>Проекты не найдены</h3>
            <p className="muted">
              {projectsPage.totalElements
                ? 'Попробуйте другой поисковый запрос.'
                : 'Создайте первый проект, чтобы начать работу с API.'}
            </p>
          </div>
        ) : (
          <>
            <div className="project-grid">
              {filteredProjects.map((project) => (
                <article
                  key={project.id}
                  className="project-card project-card--interactive"
                  role="link"
                  tabIndex={0}
                  onClick={() => navigate(`/projects/${project.id}`)}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter' || event.key === ' ') {
                      event.preventDefault();
                      navigate(`/projects/${project.id}`);
                    }
                  }}
                >
                  <div className="meta-row">
                    <span className="meta-pill">Владелец: {project.ownerUsername}</span>
                    <span className="meta-pill">ID {project.id}</span>
                    {isAdmin && scope === 'all' && <span className="meta-pill">Admin mode</span>}
                  </div>

                  <h3 className="project-card__title">{project.name}</h3>
                  <p className="muted">
                    {project.description || 'Описания пока нет. Вы можете отредактировать проект и добавить контекст.'}
                  </p>

                  <div className="project-card__footer">
                    <span className="muted">Создан: {formatDate(project.createdAt)}</span>
                  </div>

                  <div className="inline-actions" style={{ marginTop: '1rem' }}>
                    <Link
                      to={`/projects/${project.id}`}
                      className="btn btn--primary"
                      onClick={(event) => event.stopPropagation()}
                    >
                      Открыть
                    </Link>
                    <Link
                      to={`/projects/${project.id}/edit`}
                      className="btn btn--secondary"
                      onClick={(event) => event.stopPropagation()}
                    >
                      Изменить
                    </Link>
                  </div>
                </article>
              ))}
            </div>

            <div className="pagination">
              <button
                type="button"
                className="btn btn--secondary"
                onClick={() => setPage((current) => Math.max(0, current - 1))}
                disabled={page === 0}
              >
                Назад
              </button>
              <span className="muted">
                Страница {(projectsPage.number || 0) + 1} из {Math.max(projectsPage.totalPages || 1, 1)}
              </span>
              <button
                type="button"
                className="btn btn--secondary"
                onClick={() => setPage((current) => current + 1)}
                disabled={page + 1 >= Math.max(projectsPage.totalPages || 1, 1)}
              >
                Вперёд
              </button>
            </div>
          </>
        )}
      </section>
    </div>
  );
}
