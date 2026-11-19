# Flex Crew

Flex-Crew is a comprehensive full-stack web application designed to streamline team management, optimize task scheduling, and track attendance, particularly for businesses operating in flexible and remote work environments. It empowers both employees and managers with tools for efficient work organization, progress monitoring, and optimal resource allocation. The system features distinct dashboards and functionalities tailored to different user roles, facilitating a collaborative and productive workflow.

## Description

This project is a Schedule Optimization tool developed using Spring Boot (Java) for the backend and React (TypeScript) for the frontend. It aims to assist teams in finding optimal schedules within dynamic work settings and helps in maintaining attendance records. The application provides a guided process for schedule optimization and robust tracking capabilities.

**Note:** Flex-Crew is currently under active development. While core functionalities are available, some features planned for future sprints are still being implemented and refined.

## Stack

- Java JDK
  - Spring Boot
- React with TypeScript
  - [React Big calendar](https://www.npmjs.com/package/react-big-calendar)
  - Chakra UI
- PostgreSQL/MySQL/your preferred database (PostgreSQL DDL provided in `./scripts`)

## Running locally

Clone the repo and navigate to the main directory.

### Database

Execute the PostgreSQL DDL provided in `./scripts`.

### Frontend

1. Navigate to `FlexCrew-app/`.
2. Make sure you nave `node` and `npm`, then execute:

```
# Install the dependencies
npm install
```

### Backend

1. Navigate to `backend/`.
2. Create `env.properties` configuration file in `backend/src/main/resources/`. You can use `backend/src/main/resources/env.properties.example` as a template.
3. Fill in your database credentials (DB_HOST, DB_PORT, DB_DATABASE_NAME, DB_USER, DB_PASSWORD, DB_SCHEMA_NAME) and a unique `SECRET_KEY` in your local `env.properties` file.
   **Important:** The `env.properties` file is included in `.gitignore` to protect your sensitive data. Ensure your local copy is not committed.
4. Make sure you have `mvn`, then execute:

```
# Clean the previous output and compile
mvn clean && mvn package
```

## Developing

### Frontend

Run the app and write your changes:

```
# Start the dev server
npm run dev -- --host
```

### Backend

Run the backend REST API:

```
# Start the backend server
mvn spring-boot:run
```

\*\* you need to clean and recompile every change

## Building

Create the production version:

```
# You can preview the production build with npm run preview.
npm run build
```

## Backend Features

- JWT security
- Service Layer
- Password encryption
- Access token and refresh token
- Time restrictions for endpoints

## Frontend Features

- Protected routes (React router)
- JWT expiration proof axios config
- Login and register forms
- Calendar support

## Database Design

![Database Design](./images/database.PNG)

## User Interface Mockup

Here is a mockup of the user interface:

### Dashboard

![User Interface Mockup](./images/dashboard.PNG)

### Login

![Login](./images/login.png)

## Getting Started

To get started with this project, follow these steps:

1. Clone the repository.
2. Setup the database with the provided DDL in `/scripts` folder (PostgreSQL).
3. **Configuration:**
   - For the backend, copy `backend/src/main/resources/env.properties.example` to `backend/src/main/resources/env.properties`.
   - Fill in your database credentials and a `SECRET_KEY` in your local `env.properties` file.
   - **Important:** The `env.properties` file is included in `.gitignore` to protect sensitive data. Ensure your local copy is not committed if you clone this repository and make it public.
4. Run the Spring Boot application.
5. Run the React webapp.
6. Register your email.
7. Access the application.
8. Begin optimizing schedules!

## Running with Docker

`docker-compose.yaml` runs three services:

- `db`: PostgreSQL 16. On the first start of an empty volume it runs `scripts/database-creation.sql`, which creates the `Flex-Crew-v1` schema and sample data.
- `backend`: the Spring Boot API (`backend/Dockerfile`, Java 17 on Eclipse Temurin). It is not published to the host.
- `frontend`: the Vite build served by nginx (`frontend/Dockerfile`), which also proxies `/api` to the backend.

Each service has a healthcheck, and each one starts only after the service it depends on is healthy. The backend reads its configuration from environment variables, so it does not need `env.properties` in Docker.

1. Copy `.env.example` to `.env` and fill it in. `SECRET_KEY` must be a base64 key of at least 256 bits, for example from `openssl rand -base64 32`. `.env` is gitignored.
2. Build and start everything:
   ```bash
   docker compose up --build
   ```
3. Open http://localhost:5173.
4. Stop with `docker compose down`. Add `--volumes` to also delete the database volume.

The sample data is loaded only when the database volume is empty.

## License

This project is licensed under the [MIT License](LICENSE).
