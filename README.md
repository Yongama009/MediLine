# MediLine

## Run the project

### Backend

Before starting the backend, make sure PostgreSQL is running and that the `mediline` database exists.

Example setup:

```bash
sudo service postgresql start
sudo -u postgres psql -c "CREATE DATABASE mediline;"
```

Then from the `backend` folder:

```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/mediline
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
mvn clean install
mvn spring-boot:run
```

The backend runs on `http://localhost:8080` by default.

### Frontend

From the `frontend` folder:

```bash
cd frontend
python3 -m http.server 3000
```

Open `http://localhost:3000` in your browser.

> Start the backend first so the frontend can connect to `http://localhost:8080`.

## Features

The frontend supports login and registration and communicates with the backend API at `http://localhost:8080`.
