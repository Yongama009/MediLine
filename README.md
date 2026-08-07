# MediLine

## Run the project

### Backend

**Option 1: Without MySQL (Recommended for Development)**

The app uses an H2 in-memory database by default, so you can start developing right away:

```bash
cd backend
mvn spring-boot:run
```

The backend will run on `http://localhost:8080` with H2 database.

**Option 2: With MySQL**

If you want to use a real MySQL database, first set it up:

```bash
sudo service mysql start
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS mediline;"
```

Then configure the database connection:

```bash
cd backend
export DB_URL='jdbc:mysql://localhost:3306/mediline?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME=mediline
export DB_PASSWORD=mediline
mvn spring-boot:run
```

**Note:** If MySQL is not running, clear the environment variables to use H2:
```bash
unset DB_URL DB_USERNAME DB_PASSWORD DB_DRIVER DB_DIALECT
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
