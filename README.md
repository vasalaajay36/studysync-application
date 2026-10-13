# StudySync

A full-stack student productivity platform built with React, Vite, Java 21, Spring Boot, Spring Data JPA/Hibernate, and MySQL.

## Repository layout

The frontend and backend are deliberately separated:

```text
studysync-application/
├── frontend/                 # React + Vite application
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── backend/                  # Java + Spring Boot REST API
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── .mvn/
│   ├── mvnw
│   └── pom.xml
├── .github/
├── Dockerfile                # Multi-stage production build
├── railway.json
├── render.yaml
└── README.md
```

## Features

- Session-based registration, login, logout, and authenticated routes
- BCrypt password hashing
- Student-owned task, subject, study-session, and coding-profile data
- Dashboard analytics and weekly study summaries
- Pomodoro-style focus timer
- Coding profile CRUD, with automatic statistics fetching for LeetCode and Codeforces
- REST API validation and error handling
- React frontend and Spring Boot backend served from one origin in production

## Technology stack

| Layer | Technology |
|---|---|
| Frontend | React, Vite, JavaScript, HTML, CSS |
| Backend | Java 21, Spring Boot, Spring Web MVC |
| Persistence | Spring Data JPA, Hibernate, MySQL |
| Validation | Jakarta Bean Validation |
| Build | npm, Maven Wrapper |
| Deployment | Docker, Railway/Render configuration |

## Run locally

### Prerequisites

- Node.js 22 and npm
- Java 21
- MySQL, if you want to use a local database

### 1. Start the backend

From the repository root:

```bash
cd backend
./mvnw spring-boot:run
```

Configure database credentials using environment variables or your local configuration file. Do not commit credentials.

### 2. Start the frontend

Open a second terminal at the repository root:

```bash
cd frontend
npm ci
npm run dev
```

The Vite development server proxies `/api` requests to the backend. If your backend uses a port other than the configured proxy target, update `frontend/vite.config.js`.

### Build and test

Frontend:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

Backend (from the repository root):

```bash
cd backend
./mvnw clean test
./mvnw package
```

### Build the production container

Run from the repository root:

```bash
docker build -t studysync-application .
docker run --rm -p 8080:8080 --env-file .env studysync-application
```

The Docker build creates the React assets first, then packages them with the Spring Boot application. In production, frontend and API use the same origin.

## API areas

- `/api/auth` — registration, login, current user, logout
- `/api/tasks` — task management
- `/api/subjects` — subject management
- `/api/study-sessions` — study sessions
- `/api/coding-platforms` — coding profiles and supported profile-stat fetching
- `/api/dashboard` — dashboard data
- `/health` — health check

Use Postman to test protected endpoints after logging in; preserve the session cookie between requests.

## Deployment notes

The root `Dockerfile` is the deployment entry point, so a platform such as Railway can build the monorepo without setting its root directory to `backend/`. The backend packages `frontend/dist` as static resources. Configure database environment variables in the hosting provider, and verify the health check and authenticated flows after deployment.

## Security

- Never commit database passwords, API keys, tokens, or real user credentials.
- Keep production secrets in the hosting provider's environment-variable settings.
- Use HTTPS and secure session cookies in production.
- Each student's records must remain scoped to their authenticated session.

## Author

Ajay Vasala — https://github.com/vasalaajay36
