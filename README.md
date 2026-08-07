# MediLine

## Run the project

### Backend

**Start the backend (No setup required!)**

The app uses an embedded H2 file-based database that creates automatically:

```bash
cd backend
mvn spring-boot:run
```

✅ The backend will run on `http://localhost:8080` with persistent H2 database
✅ Database file stored in `data/mediline.mv.db` 
✅ Data persists between restarts

**Optional: Use MySQL instead**

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

**Tip:** To go back to H2, just unset the MySQL env vars:
```bash
unset DB_URL DB_USERNAME DB_PASSWORD DB_DRIVER DB_DIALECT
```

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
