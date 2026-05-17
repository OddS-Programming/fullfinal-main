import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  createDeployment,
  createEnvironment,
  deleteProject,
  getDeployments,
  getEnvironments,
  getGitBranches,
  getGitCommits,
  getGitWebhooks,
  getProject,
  updateGitRepository,
} from '../api';

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : 'Нет даты';
}

function statusClassName(status) {
  const normalized = (status || '').toLowerCase();

  if (normalized === 'success') {
    return 'status-badge status-badge--success';
  }

  if (normalized === 'failed') {
    return 'status-badge status-badge--failed';
  }

  return 'status-badge status-badge--pending';
}

export default function ProjectDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [project, setProject] = useState(null);
  const [environments, setEnvironments] = useState([]);
  const [deployments, setDeployments] = useState({ content: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [environmentName, setEnvironmentName] = useState('');
  const [environmentUrl, setEnvironmentUrl] = useState('');
  const [environmentDescription, setEnvironmentDescription] = useState('');
  const [deploymentVersion, setDeploymentVersion] = useState('');
  const [deploymentDescription, setDeploymentDescription] = useState('');
  const [deploymentStatus, setDeploymentStatus] = useState('SUCCESS');
  const [selectedEnvironmentId, setSelectedEnvironmentId] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const [gitRepositoryUrl, setGitRepositoryUrl] = useState('');
  const [gitBranches, setGitBranches] = useState([]);
  const [gitCommits, setGitCommits] = useState([]);
  const [gitWebhooks, setGitWebhooks] = useState({ content: [] });
  const [gitLoading, setGitLoading] = useState(false);

  const loadProjectData = async () => {
    setLoading(true);
    setError('');

    try {
      const [projectData, environmentsData, deploymentsData] = await Promise.all([
        getProject(id),
        getEnvironments(id),
        getDeployments(id, { size: 20 }),
      ]);

      setProject(projectData);
      setGitRepositoryUrl(projectData.gitRepositoryUrl || '');
      setEnvironments(environmentsData);
      setDeployments(deploymentsData);
      await loadGitData(projectData.gitRepositoryUrl);
    } catch (err) {
      setError(err.message || 'Не удалось загрузить детали проекта');
    } finally {
      setLoading(false);
    }
  };

  const loadGitData = async (repositoryUrl = gitRepositoryUrl) => {
    setGitBranches([]);
    setGitCommits([]);
    setGitWebhooks({ content: [] });

    if (!repositoryUrl) {
      return;
    }

    setGitLoading(true);
    const [branchesResult, commitsResult, webhooksResult] = await Promise.allSettled([
      getGitBranches(id),
      getGitCommits(id, { limit: 8 }),
      getGitWebhooks(id, { size: 8 }),
    ]);

    if (branchesResult.status === 'fulfilled') {
      setGitBranches(branchesResult.value);
    }
    if (commitsResult.status === 'fulfilled') {
      setGitCommits(commitsResult.value);
    }
    if (webhooksResult.status === 'fulfilled') {
      setGitWebhooks(webhooksResult.value);
    }
    setGitLoading(false);
  };

  useEffect(() => {
    loadProjectData();
  }, [id]);

  const latestDeployment = useMemo(() => {
    return deployments.content?.[0] || null;
  }, [deployments.content]);

  const handleEnvironmentSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');

    try {
      await createEnvironment(
        id,
        environmentName.trim(),
        environmentUrl.trim(),
        environmentDescription.trim(),
      );
      setEnvironmentName('');
      setEnvironmentUrl('');
      setEnvironmentDescription('');
      setSuccess('Окружение создано');
      await loadProjectData();
    } catch (err) {
      setError(err.message || 'Не удалось создать окружение');
    }
  };

  const handleDeploymentSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');

    try {
      await createDeployment(id, {
        version: deploymentVersion.trim(),
        description: deploymentDescription.trim(),
        status: deploymentStatus,
        environmentId: Number(selectedEnvironmentId),
      });
      setDeploymentVersion('');
      setDeploymentDescription('');
      setSelectedEnvironmentId('');
      setDeploymentStatus('SUCCESS');
      setSuccess('Деплой создан');
      await loadProjectData();
    } catch (err) {
      setError(err.message || 'Не удалось создать деплой');
    }
  };

  const handleDeleteProject = async () => {
    const confirmed = window.confirm('Удалить этот проект? Это действие нельзя отменить.');
    if (!confirmed) {
      return;
    }

    setIsDeleting(true);
    setError('');

    try {
      await deleteProject(id);
      navigate('/');
    } catch (err) {
      setError(err.message || 'Не удалось удалить проект');
    } finally {
      setIsDeleting(false);
    }
  };

  const handleGitSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');

    try {
      const updated = await updateGitRepository(id, gitRepositoryUrl.trim());
      setProject(updated);
      setGitRepositoryUrl(updated.gitRepositoryUrl || '');
      setSuccess('Git repository saved');
      await loadGitData(updated.gitRepositoryUrl);
    } catch (err) {
      setError(err.message || 'Cannot save git repository');
    }
  };

  if (loading) {
    return (
      <div className="stack">
        <div className="skeleton" />
        <div className="skeleton" />
      </div>
    );
  }

  if (error && !project) {
    return <div className="error-banner">{error}</div>;
  }

  if (!project) {
    return <div className="empty-state">Проект не найден.</div>;
  }

  return (
    <div className="stack">
      <section className="detail-hero">
        <div className="detail-hero__eyebrow">Детали проекта</div>
        <h1 className="detail-hero__title">{project.name}</h1>
        <p className="detail-hero__text">
          {project.description || 'Описания проекта пока нет. Используйте экран редактирования, чтобы добавить контекст.'}
        </p>

        <div className="inline-actions" style={{ marginTop: '1rem' }}>
          <Link to={`/projects/${id}/edit`} className="btn btn--primary">
            Редактировать
          </Link>
          <button type="button" className="btn btn--danger" onClick={handleDeleteProject} disabled={isDeleting}>
            {isDeleting ? 'Удаление...' : 'Удалить проект'}
          </button>
        </div>
      </section>

      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      <section className="detail-grid">
        <article className="stat-card">
          <div className="stat-card__label">Владелец</div>
          <div className="stat-card__value" style={{ fontSize: '1.5rem' }}>{project.ownerUsername}</div>
        </article>
        <article className="stat-card">
          <div className="stat-card__label">Окружения</div>
          <div className="stat-card__value">{environments.length}</div>
        </article>
        <article className="stat-card">
          <div className="stat-card__label">Загружено деплоев</div>
          <div className="stat-card__value">{deployments.content?.length || 0}</div>
        </article>
        <article className="stat-card">
          <div className="stat-card__label">Последний деплой</div>
          <div className="stat-card__value" style={{ fontSize: '1.15rem' }}>
            {latestDeployment ? latestDeployment.version : 'Нет данных'}
          </div>
        </article>
      </section>

      <section className="panel">
        <div className="panel__header">
          <div>
            <div className="section-kicker">Git</div>
            <h2 className="panel__title">Repository</h2>
          </div>
        </div>

        <form className="form-grid" onSubmit={handleGitSubmit} style={{ marginTop: '1rem' }}>
          <div className="field">
            <label htmlFor="git-repository-url">Repository URL</label>
            <input
              id="git-repository-url"
              type="url"
              value={gitRepositoryUrl}
              onChange={(event) => setGitRepositoryUrl(event.target.value)}
              placeholder="https://github.com/user/repo.git"
            />
          </div>
          <button type="submit" className="btn btn--secondary">Save Git</button>
        </form>

        {gitLoading && <div className="skeleton" style={{ marginTop: '1rem' }} />}

        <div className="detail-grid" style={{ marginTop: '1rem' }}>
          <div className="stack stack--sm">
            <h3 className="panel__title">Branches</h3>
            {gitBranches.length === 0 ? (
              <div className="empty-state">No branches loaded</div>
            ) : gitBranches.map((branch) => (
              <div key={branch.name} className="list-card">
                <strong>{branch.name}</strong>
                <div className="muted">{branch.commitHash}</div>
              </div>
            ))}
          </div>

          <div className="stack stack--sm">
            <h3 className="panel__title">Commits</h3>
            {gitCommits.length === 0 ? (
              <div className="empty-state">No commits loaded</div>
            ) : gitCommits.map((commit) => (
              <div key={commit.hash} className="list-card">
                <strong>{commit.shortHash} {commit.message}</strong>
                <div className="muted">{commit.authorName} - {formatDate(commit.committedAt)}</div>
              </div>
            ))}
          </div>

          <div className="stack stack--sm">
            <h3 className="panel__title">Webhooks</h3>
            {(gitWebhooks.content || []).length === 0 ? (
              <div className="empty-state">No webhook events</div>
            ) : gitWebhooks.content.map((event) => (
              <div key={event.id} className="list-card">
                <strong>{event.provider}: {event.eventType}</strong>
                <div className="muted">{event.deliveryId || 'no delivery id'} - {formatDate(event.createdAt)}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="panel-grid detail-grid">
        <article className="panel">
          <div className="panel__header">
            <div>
              <div className="section-kicker">Управление окружениями</div>
              <h2 className="panel__title">Окружения</h2>
            </div>
          </div>

          <div className="stack stack--sm" style={{ marginTop: '1rem' }}>
            {environments.length === 0 ? (
              <div className="empty-state">
                <h3>Окружений пока нет</h3>
                <p className="muted">Создайте хотя бы одно окружение перед добавлением деплоев.</p>
              </div>
            ) : (
              environments.map((environment) => (
                <div key={environment.id} className="list-card">
                  <strong>{environment.name}</strong>
                  <div className="muted">{environment.url || 'URL не указан'}</div>
                  {environment.description && <div className="muted">{environment.description}</div>}
                </div>
              ))
            )}
          </div>

          <form className="form-grid" onSubmit={handleEnvironmentSubmit} style={{ marginTop: '1rem' }}>
            <div className="form-grid form-grid--two">
              <div className="field">
                <label htmlFor="environment-name">Название окружения</label>
                <input
                  id="environment-name"
                  value={environmentName}
                  onChange={(event) => setEnvironmentName(event.target.value)}
                  placeholder="production"
                  required
                />
              </div>

              <div className="field">
                <label htmlFor="environment-url">URL окружения</label>
                <input
                  id="environment-url"
                  value={environmentUrl}
                  onChange={(event) => setEnvironmentUrl(event.target.value)}
                  placeholder="https://service.example.com"
                />
              </div>
            </div>

            <div className="field">
              <label htmlFor="environment-description">Описание окружения</label>
              <textarea
                id="environment-description"
                value={environmentDescription}
                onChange={(event) => setEnvironmentDescription(event.target.value)}
                maxLength={1000}
                placeholder="Например: основной production-контур для клиентов"
              />
            </div>

            <button type="submit" className="btn btn--secondary">
              Добавить окружение
            </button>
          </form>
        </article>

        <article className="panel">
          <div className="panel__header">
            <div>
              <div className="section-kicker">История деплоев</div>
              <h2 className="panel__title">Деплои</h2>
            </div>
          </div>

          <div className="stack stack--sm" style={{ marginTop: '1rem' }}>
            {(deployments.content || []).length === 0 ? (
              <div className="empty-state">
                <h3>Деплоев пока нет</h3>
                <p className="muted">Создайте запись о деплое, чтобы проверить вложенные REST endpoint.</p>
              </div>
            ) : (
              (deployments.content || []).map((deployment) => (
                <div key={deployment.id} className="deploy-row">
                  <div className="panel__header">
                    <strong>{deployment.version}</strong>
                    <span className={statusClassName(deployment.status)}>{deployment.status}</span>
                  </div>
                  {deployment.description && (
                    <div className="muted" style={{ marginTop: '0.45rem' }}>
                      {deployment.description}
                    </div>
                  )}
                  <div className="muted" style={{ marginTop: '0.45rem' }}>
                    Окружение: {deployment.environmentName}
                  </div>
                  <div className="muted">Создан: {formatDate(deployment.createdAt)}</div>
                </div>
              ))
            )}
          </div>

          <form className="form-grid" onSubmit={handleDeploymentSubmit} style={{ marginTop: '1rem' }}>
            <div className="form-grid form-grid--two">
              <div className="field">
                <label htmlFor="deployment-version">Версия</label>
                <input
                  id="deployment-version"
                  value={deploymentVersion}
                  onChange={(event) => setDeploymentVersion(event.target.value)}
                  placeholder="1.2.0"
                  required
                />
              </div>

              <div className="field">
                <label htmlFor="deployment-status">Статус</label>
                <select
                  id="deployment-status"
                  value={deploymentStatus}
                  onChange={(event) => setDeploymentStatus(event.target.value)}
                >
                  <option value="SUCCESS">SUCCESS</option>
                  <option value="FAILED">FAILED</option>
                  <option value="PENDING">PENDING</option>
                </select>
              </div>
            </div>

            <div className="field">
              <label htmlFor="deployment-description">Описание деплоя</label>
              <textarea
                id="deployment-description"
                value={deploymentDescription}
                onChange={(event) => setDeploymentDescription(event.target.value)}
                maxLength={1000}
                placeholder="Например: выкатка hotfix после исправления авторизации"
              />
            </div>

            <div className="field">
              <label htmlFor="deployment-environment">Окружение</label>
              <select
                id="deployment-environment"
                value={selectedEnvironmentId}
                onChange={(event) => setSelectedEnvironmentId(event.target.value)}
                required
              >
                <option value="">Выберите окружение</option>
                {environments.map((environment) => (
                  <option key={environment.id} value={environment.id}>
                    {environment.name}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="submit"
              className="btn btn--secondary"
              disabled={environments.length === 0}
            >
              Добавить деплой
            </button>
          </form>
        </article>
      </section>
    </div>
  );
}
