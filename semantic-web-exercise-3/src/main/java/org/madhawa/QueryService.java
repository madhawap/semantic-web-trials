package org.madhawa;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.util.LinkedList;

//http://localhost:8080/semantic_web_exercise_3/rest/whois/Berners%20Lee
//Turing
@Path("/whois")
public class QueryService {
    @GET
    @Path("/{name}")
    @Produces("text/plain")
    public Response getMsg(@PathParam("name") String name) {
        //ToDo: There is a possibility of an array out of bound exception. Need to handle the exception
        //ToDo: Make the @Produces type to 'application/json' to handle multiple outputs
        LinkedList<String> names = readOntology(name);
        String output = names.get(0);
        return Response.status(200).entity(output).build();
    }

    private LinkedList<String> readOntology(String name) {
        LinkedList<String> names = new LinkedList<>();

        Model mdl = FileManager.get().loadModel("/Users/madhawaperera/Documents/GitHub/university.rdf");

        String qry =
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "prefix university: <http://university.owl#>" +
                        "SELECT ?x WHERE {" +
                        "?person university:lName'" + name + "' ." +
                        "?person university:fName ?x ." +
                        "}";
        Query query = QueryFactory.create(qry);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, mdl)) {
            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                QuerySolution sol = rs.nextSolution();
                Literal ans = sol.getLiteral("x");
                names.add(ans.getString());
            }
            return names;
        }

    }
}
