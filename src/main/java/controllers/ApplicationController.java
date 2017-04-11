package controllers;

import com.google.inject.Inject;
import com.jayway.jsonpath.ReadContext;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import jooq.tables.records.TodoRecord;
import model.Todo;
import org.jooq.DSLContext;

import java.util.List;

import static jooq.tables.Todo.TODO;

public class ApplicationController {
    @Inject private DSLContext create;

    public Response getAll(Request r) {
        List<Todo> result = create.selectFrom(TODO).fetchInto(Todo.class);

        result.stream().forEach(todo -> todo.url = composeUrl(r, todo.id));

        return Response.withOk().andJsonBody(result);
    }

    public Response create(Request r) {

        ReadContext ctx = r.getBodyAsJsonPath();

        String title = ctx.read("$.title");


        TodoRecord tr = create.insertInto(TODO).columns(TODO.TITLE).values(title).returning(TODO.ID).fetchOne();

        Todo t = new Todo();
        t.id = tr.get(TODO.ID);
        t.title = title;
        t.url = composeUrl(r, t.id);

        return Response.withOk().andJsonBody(t);
    }

    public Response deleteAll() {
        create.deleteFrom(TODO).execute();

        return Response.withOk().andEmptyBody();
    }

    public Response options() {
        return Response.withOk().andEmptyBody();
    }


    private String composeUrl(Request r, long id) {
        return (r.getURL().endsWith("/")) ? r.getURL() + id : r.getURL() + "/" + id;
    }

    //POST: {"title":"a todo"}
    //{"title":"a todo","completed":false,"url":"http://todobackend-spring.herokuapp.com//28","order":null}

    //DELETE: / - deletes all todos
    //

    //GET: / - returns all as an array
    //[{"id":1,"title":"walk the dog","completed":false,"order":0,"url":"https://todo-backend-spring4-java8.herokuapp.com/todos/1"}]
}
