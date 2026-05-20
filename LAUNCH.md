# Launch Guide

Короткая и практическая инструкция по запуску `DevOps Panel`.

> [!TIP]
> Самый простой и предсказуемый вариант: backend и PostgreSQL через Docker, frontend через Vite.

## Quick Launch

Из корня проекта:

```bash
docker compose up --build -d
```

Проверить статус:

```bash
docker compose ps
```

Ожидаемо:

- `devops-panel-app` в статусе `Up`
- `devops-panel-db` в статусе `Up`

## Frontend

В отдельном терминале:

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

Открыть:

- `http://127.0.0.1:5173`

## Backend

После Docker-запуска backend доступен по адресу:

- `http://localhost:8081`

Проверка API без авторизации:

```bash
curl http://localhost:8081/api/projects
```

Ответ `401` или `403` без токена здесь нормален.

## Admin Login

Seed admin создаётся автоматически:

| Login | Password |
| --- | --- |
| `admin` | `password` |

## Manual Smoke Test

1. Открыть `http://127.0.0.1:5173`.
2. Войти как `admin / password`.
3. Проверить список проектов.
4. Создать проект.
5. Открыть его кликом по карточке.
6. Добавить окружение с описанием.
7. Добавить деплой с описанием.
8. Проверить админский режим `Все проекты / Мои проекты`.

## Local Backend Without Docker

Нужны:

- Java 17
- Maven

По умолчанию используется in-memory H2 база данных, поэтому PostgreSQL локально не требуется.

Запуск:

```bash
mvn spring-boot:run
```

Если вы хотите использовать PostgreSQL, задайте переменные окружения, например:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/devops_panel"
$env:DB_DRIVER="org.postgresql.Driver"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
mvn spring-boot:run
```

## Validation Commands

Backend tests:

```bash
mvn test
```

Frontend build:

```bash
cd frontend
npm.cmd run build
```

## Stop

Docker stack:

```bash
docker compose down
```

Vite dev server or local backend:

- `Ctrl + C` в соответствующем терминале
