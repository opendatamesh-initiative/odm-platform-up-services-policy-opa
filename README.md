# Open Data Mesh Plane Utility Policyservice OPA

Open Data Mesh Platform is a platform that manages the full lifecycle of a data product from deployment to retirement. It use the [Data Product Descriptor Specification](https://dpds.opendatamesh.org/) to to create, deploy and operate data product containers in a mesh architecture. This repository contains the services exposed by the utility policyservice plane.

# Run it

## Prerequisites
The project requires the following dependencies:

* Java 11
* Maven 3.8.6

## Run locally

### Clone repository
Clone the repository and move to the project root folder

```bash
git git clone git@bitbucket.org:quantyca/odm-plane-utility-policyservice-opa.git
cd odm-plane-utility-policyservice-opa
```

### Compile project
Compile the project:

```bash
mvn clean install
```

### Run application
Run the application:

```bash
java -jar target/policyservice-opa.0.0.1-SNAPSHOT.jar
```

### Stop application
To stop the application type CTRL+C or just close the shell. To start it again re-execute the following command:

```bash
java -jar target/policyservice-opa.0.0.1-SNAPSHOT.jar
```
*Note: The application run in this way uses an in-memory instance of the H2 database. For this reason, the data is lost every time the application is terminated. On the next restart, the database is recreated from scratch.*

*Note: The application need a reachable OPA server listening on port 8181 to correctly work*

## Run with Docker

### Clone repository
Clone the repository and move it to the project root folder

```bash
git git clone git@bitbucket.org:quantyca/odm-plane-utility-policyservice-opa.git
cd odm-plane-utility-policyservice-opa
```

Here you can find the following two Dockerfiles:
* `Dockerfile`: This file creates a docker image containing the application built from the code present on the Git repository;
* `Dockerfile.local`: This file creates an image containing the application by directly copying it from the build executed locally (i.e. from `target` folder).

### Compile project
If you decide to create the Docker image using the second Dockerfile (i.e. `Dokerfile.local`), you need to first execute the build locally by running the following command:

```bash
mvn clean install
```

### Run OPA server
The image generated from both Dockerfiles contains only the application. It requires an OPA server to run properly. If you do not already have an OPA server available, you can create one by running the following commands:

```bash
docker run --name odmopa-opa-server -d -p 8181:8181  \
   openpolicyagent/opa:latest-rootless \
   run \
   --server \
   --log-level=debug  \
   --log-format=json-pretty \
   --set=decision_logs.console=true
```

Check that the OPA server has started correctly:
```bash
docker logs odmopa-opa-server
```

### Run database
The image generated from both Dockerfiles contains only the application. It requires a database to run properly. The supported databases are MySql and Postgres. If you do not already have a database available, you can create one by running the following commands:

**MySql**
```bash
docker run --name odmopa-mysql-db -d -p 3306:3306  \
   -e MYSQL_DATABASE=odmopadb \
   -e MYSQL_ROOT_PASSWORD=root \
   mysql:8
```

**Postgres**
```bash
docker run --name odmopa-postgres-db -d -p 5432:5432  \
   -e POSTGRES_DB=odmopadb \
   -e POSTGRES_PASSWORD=postgres \
   postgres:11-alpine
```

Check that the database has started correctly:

**MySql**
```bash
docker logs odmopa-mysql-db
```

**Postgres**
```bash
docker logs odmopa-mysql-db
```
### Build image
Build the Docker image of the application and run it.

*Before executing the following commands:
* change properly the value of arguments `DATABASE_USERNAME`, `DATABASE_PASSWORD` and `DATABASE_URL`. Reported commands already contains right argument values if you have created the database using the commands above.
* remove the option `-f Dockerfile.local` if you want to build the application from code taken from repository*

**MySql**
```bash
docker build -t odmopa-mysql-app . -f Dockerfile.local \
   --build-arg DATABASE_URL=jdbc:mysql://localhost:3306/odmopadb \
   --build-arg DATABASE_USERNAME=root \
   --build-arg DATABASE_PASSWORD=root \
   --build-arg FLYWAY_SCRIPTS_DIR=mysql
```

**Postgres**
```bash
docker build -t odmopa-postgres-app . -f Dockerfile.local \
   --build-arg DATABASE_URL=jdbc:postgresql://localhost:5432/odmopadb \
   --build-arg DATABASE_USERNAME=postgres \
   --build-arg DATABASE_PASSWORD=postgres \
   --build-arg FLYWAY_SCRIPTS_DIR=postgres
```

### Run application
Run the Docker image.

*Note: Before executing the following commands remove the argument `--net host` if the database is not running on `localhost`*

**MySql**
```bash
docker run --name odmopa-mysql-app -p 4242:4242 --net host odmopa-mysql-app
```

**Postgres**
```bash
docker run --name ododmopa-postgres-appmp -p 4242:4242 --net host odmopa-postgres-app
```

### Stop application

*Before executing the following commands:
* change the instance name to `odmopa-postgres-app` if you are using postgres and not mysql *

```bash
docker stop odmopa-mysql-app
docker stop odmopa-mysql-db
docker stop odmopa-opa-server
```
To restart a stopped application execute the following commands:

```bash
docker start odmopa-opa-server
docker start odmopa-mysql-db
docker start odmopa-mysql-app
```

To remove a stopped application to rebuild it from scratch execute the following commands :

```bash
docker rm odmopa-mysql-app
docker rm odmopa-mysql-db
docker rm odmopa-opa-server
```

## Run with Docker Compose

### Clone repository
Clone the repository and move it to the project root folder

```bash
git git clone git@bitbucket.org:quantyca/odm-plane-utility-policyservice-opa.git
cd odm-plane-utility-policyservice-opa
```

### Build image
Build the docker-compose images of the application, a default OPA server and a default PostgreSQL DB.

Before building it, create a `.env` file in the root directory of the project similar to the following one:
```.dotenv
OPA_PORT=8181
OPA_LOG_LEVEL=debug
JAVA_OPTS=
SPRING_PROFILES_ACTIVE=docker
SPRING_PORT=4242
DATABASE_PORT=5433
DATABASE_NAME=mydb
DATABASE_USERNAME=usr
DATABASE_PASSWORD=pwd
```

Then, build the docker-compose file:
```bash
docker-compose build
```

### Run application
Run the docker-compose images.
```bash
docker-compose up
```

### Stop application
Stop the docker-compose images
```bash
docker-compose down
```
To restart a stopped application execute the following commands:

```bash
docker-compose up
```

To rebuild it from scratch execute the following commands :
```bash
docker-compose build --no-cache
```

# Test it

## REST services

You can invoke REST endpoints through *OpenAPI UI* available at the following url:

* [http://localhost:4242/api/v1/planes/utility/policy-services/opa/swagger-ui/index.html](http://localhost:4242/api/v1/planes/utility/policy-services/opa/swagger-ui/index.html)

## Database

If the application is running using an in memory instance of H2 database you can check the database content through H2 Web Console available at the following url:

* [http://localhost:4242/api/v1/planes/utility/policy-services/opa//h2-console](http://localhost:4242/api/v1/planes/utility/policy-services/opa/h2-console)

In all cases you can also use your favourite sql client providing the proper connection parameters