package codemwnci

import javax.ws.rs.*
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

import kotliquery.*
import javax.inject.Inject
import javax.enterprise.inject.Default
import javax.sql.DataSource

@Path("/todos")
class TodoResource {

    class Todo(val id: Long, val txt: String, val completed: Boolean = false)
    
    @Inject
    @field: Default 
    lateinit var ds: DataSource

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // fun getAll() = todos
    fun getAll() = using(sessionOf(ds)) { session ->  
        session.run(queryOf("SELECT id, txt, completed FROM todo").map { 
            row -> Todo(row.long("id"), row.string("txt"), row.boolean("completed"))
        }.asList)
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    // fun getOne(@PathParam("id") id: Int) = todos.first { it.id == id }
    fun getOne(@PathParam("id") id: Long) = using(sessionOf(ds)) { session ->  
        session.run(queryOf("SELECT id, txt, completed FROM todo WHERE id=?", id).map { 
            row -> Todo(row.long("id"), row.string("txt"), row.boolean("completed"))
        }.asSingle)
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    //fun deleteOne(@PathParam("id") id: Long) = todos.remove( todos.first { it.id == id } )
    fun deleteOne(@PathParam("id") id: Long) {
        using(sessionOf(ds)) { session -> session.run(queryOf("DELETE FROM todo WHERE id=?", id).asUpdate)}
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun addOne(txt: String): Todo? {
        val newId: Long? = using(sessionOf(ds, true)) { session -> session.run(queryOf("INSERT INTO todo (txt) VALUES (?)", txt).asUpdateAndReturnGeneratedKey )}
        return if (newId != null) getOne(newId) else null
    }


    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun updateOne(@PathParam("id") id: Long, todo: Todo): Todo? {
        using(sessionOf(ds)) { session -> session.run(queryOf("UPDATE todo SET txt=?, completed=? WHERE id=?", todo.txt, todo.completed, id).asUpdate)}
        return getOne(id)
    }
}