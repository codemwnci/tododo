# NOTES
The following will run an interactive* Postgres database  (*will die with Ctrl+C)
docker run -it --rm --name pg-docker -e POSTGRES_USER=pg -e POSTGRES_PASSWORD=pg -e POSTGRES_DB=todo -p 5432:5432 postgres

You can then connect to it using psql
docker run -it --rm postgres psql -h host.docker.internal -U pg -d todo

If you would rather not use flyway to provision the database tables, from within PSQL, you can then run the create table commands yourself.
(see /src/main/resources/db/migration/v0.1__initial.sql)


# Testing Notes
ADD: curl -d 'curl created todo to database' -H "Content-Type: application/json" -X POST http://localhost:8080/todos
GET: curl http://localhost:8080/todos/1
ADD: curl -d 'curl created todo to database #2' -H "Content-Type: application/json" -X POST http://localhost:8080/todos
GETALL: curl http://localhost:8080/todos
DELETE: curl -X DELETE http://localhost:8080/todos/1
GETALL: curl http://localhost:8080/todos
ADD: curl -d 'curl created todo to database #3' -H "Content-Type: application/json" -X POST http://localhost:8080/todos
DELETE: curl -X DELETE http://localhost:8080/todos/4
UPDATE: curl -d '{"id":5,"txt":"curl created todo to database #5","completed":true}' -H "Content-Type: application/json" -X PUT http://localhost:8080/todos/5
GETALL: curl http://localhost:8080/todos


# Quarkus notes
Remember to add Jackson Kotlin Module, otherwise Kotlin has issues with JSON serialisation from the default Java modules

# GraalVM Notes
https://medium.com/@jitihn/quarkus-building-a-native-executable-on-ubuntu-20c3446afc86



# Testing notes
RestAssured does not run javascript, so it will not be suitable for UI tests, just rest api tests.
To ensure the H2 database is injected for tesing, make sure to add the following dependencies
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-jdbc-h2</artifactId>
      <scope>test</scope>
    </dependency>      
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-test-h2</artifactId>
      <scope>test</scope>
    </dependency>


Add the following to the top of the test class (not the ::class for kotlin, and the long TestInstance.Lifecycle.PER_CLASS)
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.h2.H2DatabaseTestResource

@QuarkusTestResource(H2DatabaseTestResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)



# todo-server project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application is packageable using `./mvnw package`.
It produces the executable `todo-server-1.0-SNAPSHOT-runner.jar` file in `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/todo-server-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or you can use Docker to build the native executable using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your binary: `./target/todo-server-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image-guide .