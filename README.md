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
