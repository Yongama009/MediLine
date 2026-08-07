# MediLine

## Run the project

### Backend

Before starting the backend, make sure MySQL is running and that the `mediline` database exists.

Example setup:

```bash
sudo service mysql start
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS mediline;"
```

Then from the `backend` folder:

```bash
cd backend
export DB_URL='jdbc:mysql://localhost:3306/mediline?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME=mediline
export DB_PASSWORD=mediline
mvn clean install
mvn spring-boot:run
```

If your local MySQL server does not accept those credentials, the app will still start with its built-in in-memory fallback so you can continue developing.


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
