package codemwnci

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.*

import org.hamcrest.CoreMatchers.containsString;
import org.hamcrest.core.IsNot.not;

import kotliquery.*
import javax.inject.Inject
import javax.enterprise.inject.Default
import javax.sql.DataSource

import org.flywaydb.core.Flyway
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.h2.H2DatabaseTestResource

@QuarkusTestResource(H2DatabaseTestResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
open class TodoResourceTest {

    @Inject
    @field: Default 
    lateinit var ds: DataSource

    @Inject
    lateinit var flyway: Flyway;

    // Create the DB structure before each test
    @BeforeEach
    fun dropAndRecreate() {
            flyway.clean(); 
            flyway.migrate()
        // using(sessionOf(ds)) { session -> 
        //     //session.run(queryOf("DELETE FROM todo;").asUpdate)
        //     //session.run(queryOf("ALTER SEQUENCE todo_id_seq RESTART with 1;").asUpdate)
        //     // session.run(queryOf("DROP TABLE IF EXISTS todo;").asUpdate)
        //     // session.run(queryOf("CREATE TABLE todo (id serial PRIMARY KEY, txt VARCHAR NOT NULL, completed boolean default false);").asUpdate)
        // }
    }
    
    fun addTestTodos(vararg todos: String) {
        using(sessionOf(ds)) { session -> 
            for(todo in todos) {
                session.run(queryOf("INSERT INTO todo (txt) VALUES (?);", todo).asUpdate)
            }
        }
    }

    @Test
    fun testFirstTodo() {

            given()
              .`when`().get("/todos")
              .then()
              .statusCode(200)
              .body(containsString("[]"))
    }


    @Test
    fun testAdd() {

            // TEST THE POST RETURNS A SINGLE JSON 
            given()
              .body("test todo")
              .`when`().post("/todos")
              .then()
              .statusCode(200)
              .body(containsString("test todo"), containsString("""{"id":1,"txt":"test todo","completed":false}"""))


            // TEST GET NOW RETURNS AN ARRAY WITH A SINGLE ITEM
            given()
              .`when`().get("/todos")
              .then()
              .statusCode(200)
              .body(containsString("""[{"id":1,"txt":"test todo","completed":false}]"""))
    }

    @Test
    fun testGetOne() {
        addTestTodos("number1", "test 2")
        given()
            .`when`().get("/todos/1")
            .then()
            .statusCode(200)
            .body(containsString("""{"id":1,"txt":"number1","completed":false}"""), not(containsString("test 2")))    
    }


    @Test
    fun testDelete() {
        addTestTodos("number1", "test 2")

        // confirm the item is there
        given()
            .`when`().get("/todos/1")
            .then()
            .statusCode(200)
            .body(containsString("""{"id":1,"txt":"number1","completed":false}"""), not(containsString("test 2")))    

        // confirm it is no longer there
        given()
            .`when`().delete("/todos/1")
            .then()
            .statusCode(204)
            .body(not(containsString("number1")))

          given()
            .`when`().get("/todos/1")
            .then()
            .statusCode(204)
            .body(not(containsString("number1")))    
    }

    
    @Test
    fun testUpdate() {
        addTestTodos("number1", "test 2")

         // confirm the item is there
        given()
            .`when`().get("/todos/1")
            .then()
            .statusCode(200)
            .body(containsString("""{"id":1,"txt":"number1","completed":false}"""), not(containsString("test 2")))  

        
        // confirm it returned the updated details
        given()
            .contentType("application/json")
            .body("""{"id":1,"txt":"number111","completed":true}""")
            .`when`().put("/todos/1")
            .then()
            .statusCode(200)
            .body(not(containsString("\"number1\"")), containsString("number111"), containsString("true"))

        // confirm it returns fron get as expected
        given()
            .`when`().get("/todos/1")
            .then()
            .statusCode(200)
            .body(containsString("""{"id":1,"txt":"number111","completed":true}"""))  



        // make sure we cant change the ID and that it returned the updated details
        given()
            .contentType("application/json")
            .body("""{"id":1111,"txt":"number122","completed":true}""")
            .`when`().put("/todos/1")
            .then()
            .statusCode(200)
            .body(not(containsString("\"number1\"")), containsString("number122"), containsString("true"), not(containsString("1111")))

        // confirm it returns fron get as expected
        given()
            .`when`().get("/todos/1")
            .then()
            .statusCode(200)
            .body(containsString("""{"id":1,"txt":"number122","completed":true}"""))  



    }

}