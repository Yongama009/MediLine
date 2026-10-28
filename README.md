# MediLine

## Run the project

### Backend

**Start the backend with MySQL**

Make sure MySQL is running locally and that a database named `mediline` exists:

```bash
sudo service mysql start
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS mediline; CREATE USER IF NOT EXISTS 'mediline'@'localhost' IDENTIFIED BY 'mediline'; GRANT ALL PRIVILEGES ON mediline.* TO 'mediline'@'localhost'; FLUSH PRIVILEGES;"
```

Then start the backend:

```bash
cd backend
export DB_URL='jdbc:mysql://localhost:3306/mediline?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME=mediline
export DB_PASSWORD=mediline
export ADMIN_REGISTRATION_CODE='choose-a-private-code'
mvn spring-boot:run
```

The backend runs on `http://localhost:8080` and uses MySQL by default.

## Deploy the backend to AWS Elastic Beanstalk

The backend includes the AWS SDK for S3 and an Elastic Beanstalk `Procfile`.
S3 operations are not currently implemented in the application.

1. Create an Elastic Beanstalk web application using the Java 21 platform.
2. Build an Elastic Beanstalk source bundle from the backend:

   ```bash
   cd backend
   mvn clean package
   mkdir -p target/eb-bundle
   cp target/queue-1.0.0-SNAPSHOT.jar target/eb-bundle/application.jar
   cp Procfile target/eb-bundle/Procfile
   (cd target/eb-bundle && zip -r ../mediline-eb.zip .)
   ```

3. Upload `backend/target/mediline-eb.zip` as an application version and deploy it
   to an environment.
4. Configure the environment properties in Elastic Beanstalk:
   - `DB_URL`: JDBC URL for an AWS-hosted MySQL database such as Amazon RDS
   - `DB_USERNAME` and `DB_PASSWORD`: database credentials
   - `ADMIN_REGISTRATION_CODE`: a private registration code
   - `CORS_ALLOWED_ORIGINS`: the deployed frontend origin

   Do not expose the database publicly; allow connections from the Elastic
   Beanstalk environment's security group. Keep secrets in Elastic Beanstalk
   environment properties or AWS Secrets Manager, not in source control.

The service listens on port `5000` in the AWS deployment. Flyway migrations
are packaged from `database/migrations/` and run on application startup.
When the application begins using S3, grant its Elastic Beanstalk EC2 instance
profile only the required bucket permissions; do not put AWS access keys in
the application configuration.

**Optional: use H2 locally**

If you want to switch back to the embedded H2 database for local testing, set the following before running:

```bash
export DB_URL='jdbc:h2:./data/mediline;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
export DB_DRIVER=''
export DB_USERNAME='sa'
export DB_PASSWORD=''
export DB_DIALECT='org.hibernate.dialect.H2Dialect'
```

## Separate frontend

The frontend is maintained in the sibling `MediLine-front` repository. Start it
in a separate terminal after starting the backend:

```bash
cd ../MediLine-front/frontend
python3 -m http.server 3000
```

Open `http://localhost:3000`. The frontend calls the backend at
`http://localhost:8080/api`; the backend allows `http://localhost:3000` through
CORS by default. The frontend should communicate with the backend over HTTP and
should never connect directly to MySQL.

WTC-SQKPVED4
