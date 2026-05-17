import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createProject, getProject, updateProject } from '../api';

export default function ProjectForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [publicProject, setPublicProject] = useState(false);
  const [gitRepositoryUrl, setGitRepositoryUrl] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(isEdit);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!isEdit) {
      return;
    }

    let isActive = true;
    setLoading(true);

    getProject(id)
      .then((project) => {
        if (isActive) {
          setName(project.name);
          setDescription(project.description || '');
          setPublicProject(Boolean(project.publicProject));
          setGitRepositoryUrl(project.gitRepositoryUrl || '');
        }
      })
      .catch((err) => {
        if (isActive) {
          setError(err.message || 'Не удалось загрузить проект');
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
  }, [id, isEdit]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      if (isEdit) {
        await updateProject(id, { name, description, publicProject, gitRepositoryUrl });
        navigate(`/projects/${id}`);
      } else {
        const created = await createProject({ name, description, publicProject, gitRepositoryUrl });
        navigate(`/projects/${created.id}`);
      }
    } catch (err) {
      setError(err.message || 'Не удалось сохранить проект');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="stack">
      <section className="detail-hero">
        <div className="detail-hero__eyebrow">{isEdit ? 'Редактирование проекта' : 'Создание проекта'}</div>
        <h1 className="detail-hero__title">
          {isEdit ? 'Обновите данные проекта и подготовьте его к новым деплоям.' : 'Создайте новый проект для вашего процесса поставки.'}
        </h1>
        <p className="detail-hero__text">
          Эта форма напрямую работает с защищёнными endpoint проекта и показывает ошибки валидации backend, если данные некорректны.
        </p>
      </section>

      <section className="panel">
        {loading ? (
          <div className="skeleton" />
        ) : (
          <form className="form-grid" onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="project-name">Название проекта</label>
              <input
                id="project-name"
                type="text"
                value={name}
                onChange={(event) => setName(event.target.value)}
                maxLength={200}
                required
              />
            </div>

            <div className="field">
              <label htmlFor="project-description">Описание</label>
              <textarea
                id="project-description"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                maxLength={1000}
                placeholder="Что делает этот сервис и почему он важен?"
              />
            </div>

            <div className="field">
              <label htmlFor="project-git-url">Git repository URL</label>
              <input
                id="project-git-url"
                type="url"
                value={gitRepositoryUrl}
                onChange={(event) => setGitRepositoryUrl(event.target.value)}
                maxLength={500}
                placeholder="https://github.com/user/repo.git"
              />
            </div>

            <label className="field" htmlFor="project-public">
              <span>Public project</span>
              <input
                id="project-public"
                type="checkbox"
                checked={publicProject}
                onChange={(event) => setPublicProject(event.target.checked)}
              />
            </label>

            {error && <div className="error-banner">{error}</div>}

            <div className="inline-actions">
              <button type="submit" className="btn btn--primary" disabled={isSubmitting}>
                {isSubmitting ? 'Сохранение...' : isEdit ? 'Сохранить изменения' : 'Создать проект'}
              </button>
              <button type="button" className="btn btn--secondary" onClick={() => navigate(-1)}>
                Отмена
              </button>
            </div>
          </form>
        )}
      </section>
    </div>
  );
}
