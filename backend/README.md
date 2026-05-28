# Backend

This folder contains the backend application scaffold.

## Run locally

1. Start PostgreSQL locally and ensure a database named `mediline` exists.
   - If PostgreSQL is installed as a local service, start it with:

```bash
sudo service postgresql start
```

   - Create the database if needed:

```bash
sudo -u postgres psql -c "CREATE DATABASE mediline;"
```

2. Set the database environment variables before starting the app:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/mediline
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

3. Open a terminal in the `backend` folder.
4. Start the Spring Boot application:

```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`.

### Database migrations

Flyway migrations are stored in `database/migrations/` and run automatically on startup.

### API endpoints

- `POST /api/patients` for patient registration
- `GET /api/patients` to list patients
- `GET /api/patients/{id}` to fetch one patient
- `PUT /api/patients/{id}` to update a patient
- `DELETE /api/patients/{id}` to remove a patient
