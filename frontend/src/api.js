const API_BASE = '/api';

export function getStoredAuth() {
  return {
    token: localStorage.getItem('token'),
    username: localStorage.getItem('username'),
    role: localStorage.getItem('role'),
    userId: localStorage.getItem('userId'),
  };
}

export function storeAuth(data) {
  localStorage.setItem('token', data.token);
  localStorage.setItem('username', data.username);
  localStorage.setItem('role', data.role || 'USER');
  localStorage.setItem('userId', String(data.userId ?? ''));
}

export function clearAuth() {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
  localStorage.removeItem('userId');
}

export async function api(url, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  const token = getStoredAuth().token;
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${url}`, { ...options, headers });
  const isJson = response.headers.get('content-type')?.includes('application/json');
  const data = isJson ? await response.json().catch(() => ({})) : null;

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      clearAuth();
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
    }
    const error = new Error(
      data?.message || data?.error || `Запрос завершился с ошибкой ${response.status}`,
    );
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

export async function register(username, email, password) {
  return api('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, email, password }),
  });
}

export async function login(username, password) {
  return api('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}

export async function getProjects(params = {}) {
  const query = new URLSearchParams(params).toString();
  return api(`/projects${query ? `?${query}` : ''}`);
}

export async function getMyProjects(params = {}) {
  const query = new URLSearchParams(params).toString();
  return api(`/projects/my${query ? `?${query}` : ''}`);
}

export async function getPublicProjects(params = {}) {
  const query = new URLSearchParams(params).toString();
  return api(`/projects/public${query ? `?${query}` : ''}`);
}

export async function getProject(id) {
  return api(`/projects/${id}`);
}

export async function createProject(name, description) {
  return api('/projects', {
    method: 'POST',
    body: JSON.stringify(name && typeof name === 'object'
      ? name
      : { name, description: description || '' }),
  });
}

export async function updateProject(id, name, description) {
  return api(`/projects/${id}`, {
    method: 'PUT',
    body: JSON.stringify(name && typeof name === 'object'
      ? name
      : { name, description: description || '' }),
  });
}

export async function deleteProject(id) {
  return api(`/projects/${id}`, { method: 'DELETE' });
}

export async function getEnvironments(projectId) {
  return api(`/projects/${projectId}/environments/list`);
}

export async function createEnvironment(projectId, name, url, description) {
  return api(`/projects/${projectId}/environments`, {
    method: 'POST',
    body: JSON.stringify({ name, url: url || '', description: description || '' }),
  });
}

export async function getDeployments(projectId, params = {}) {
  const query = new URLSearchParams(params).toString();
  return api(`/projects/${projectId}/deployments${query ? `?${query}` : ''}`);
}

export async function createDeployment(projectId, payload) {
  return api(`/projects/${projectId}/deployments`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function updateGitRepository(projectId, repositoryUrl) {
  return api(`/projects/${projectId}/git/repository`, {
    method: 'PUT',
    body: JSON.stringify({ repositoryUrl }),
  });
}

export async function getGitBranches(projectId) {
  return api(`/projects/${projectId}/git/branches`);
}

export async function getGitCommits(projectId, params = {}) {
  const query = new URLSearchParams(params).toString();
  return api(`/projects/${projectId}/git/commits${query ? `?${query}` : ''}`);
}

export async function getGitWebhooks(projectId, params = {}) {
  const query = new URLSearchParams(params).toString();
  return api(`/projects/${projectId}/git/webhooks${query ? `?${query}` : ''}`);
}
