import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class SPARQLExecuter {

    void readOntology(String location) {
        FileManager.get().addLocatorClassLoader(SPARQLExecuter.class.getClassLoader());
        Model mdl = FileManager.get().loadModel(location);

        String qry =
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "prefix university: <http://university.owl#>" +
                        "SELECT ?x WHERE {" +
                        "?person university:lName'Berners Lee' ." +
                        "?person university:fName ?x ." +
                        "}";
        Query query = QueryFactory.create(qry);
        QueryExecution qexec = QueryExecutionFactory.create(query, mdl);
        try {
            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                QuerySolution sol = rs.nextSolution();
                Literal ans = sol.getLiteral("x");
                System.out.println(ans);
            }
        } finally {
            qexec.close();
        }

    }

    void insertOntology(String location) throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(location);
        String NS = "http://university.owl#";

        Resource r = m.createResource("http://university.owl#Lect3")
                .addProperty(RDF.type, m.getResource(NS + "Lecturer")); //subject


        Property p1 = m.createProperty(NS + "staff_id");  //predicates
        Property p2 = m.createProperty(NS + "fName");
        Property p3 = m.createProperty(NS + "lName");


        r.addProperty(p1, "100", XSDDatatype.XSDint); // objects
        r.addProperty(p2, "Alan", XSDDatatype.XSDstring);
        r.addProperty(p3, "Turing", XSDDatatype.XSDstring);


        m.write(new FileOutputStream(location), "RDF/XML-ABBREV");

    }

    public void insertOntology2(String location) throws FileNotFoundException {
    }

    public void updateOntology(String location) throws FileNotFoundException {
    }

    public void deleteOntology() throws FileNotFoundException {
    }

}


