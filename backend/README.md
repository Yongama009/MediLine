# Backend

This folder contains the backend application scaffold.

## Run locally

1. Start MySQL locally and ensure a database named `mediline` exists.
   - If MySQL is installed as a local service, start it with:

```bash
sudo service mysql start
```

   - Create the database if needed:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS mediline;"
```

2. Set the database environment variables before starting the app:

```bash
export DB_URL='jdbc:mysql://localhost:3306/mediline?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME=mediline
export DB_PASSWORD=mediline
```

3. Open a terminal in the `backend` folder.
4. Build and start the Spring Boot application:

```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080` by default.

> If port `8080` is already in use, run with an alternate port:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Database migrations

Flyway migrations are stored in `../database/migrations/`, packaged into the
application JAR, and run automatically on startup.

### AWS Elastic Beanstalk

The project includes the AWS SDK for S3 and a `Procfile` for Elastic Beanstalk's
Java 21 platform. To create a source bundle, build the JAR and place it with the
Procfile at the bundle root:

```bash
mvn clean package
mkdir -p target/eb-bundle
cp target/queue-1.0.0-SNAPSHOT.jar target/eb-bundle/application.jar
cp Procfile target/eb-bundle/Procfile
(cd target/eb-bundle && zip -r ../mediline-eb.zip .)
```

Upload `target/mediline-eb.zip` as an Elastic Beanstalk application version.
Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_REGISTRATION_CODE`,
and `CORS_ALLOWED_ORIGINS` in the environment. The database must be reachable
from the environment, and production secrets should not be committed to Git.

S3 client operations are not yet implemented. When adding them, use the
Elastic Beanstalk instance profile for AWS credentials and grant it only the
permissions the application needs.

### API endpoints

- `POST /api/patients` for patient registration
- `GET /api/patients` to list patients
- `GET /api/patients/{id}` to fetch one patient
- `PUT /api/patients/{id}` to update a patient
- `DELETE /api/patients/{id}` to remove a patient
