import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.vandermeer.asciitable.AsciiTable;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RDFAnalyser {

    public static void main(String[] args) {

        SPARQLExecuter sparqlExecuter = new SPARQLExecuter();
        // Repeatedly requests for User input until "E" ro "e" pressed
        do {
            String location = getFileFromUser();
            if (location.equals("e") || location.equals("E")) {
                break;
            } else {
                int outputType = getAlgorithmType();

                if (outputType == 1) {
                    try {
                        outputType1(location);
                    } catch (ParserConfigurationException e) {
                        System.err.println("Error loading RDF document \n" + e);
                    }
                } else if (outputType == 2) {
                    sparqlExecuter.readOntology(location);
                } else if (outputType == 3) {
                    try {
                        sparqlExecuter.insertOntology(location);
                    } catch (FileNotFoundException e) {
                        System.err.println("Error when finding file");
                    }
                }
            }
        } while (true);
    }

    private static String getFileFromUser() {
        System.out.print("Enter location of RDF file or press E or e to exit the program: ");
        Scanner scanner = new Scanner(System.in);

        return scanner.next();
    }

    private static Integer getAlgorithmType() {
        String newLine = System.getProperty("line.separator");
        System.out.println("-------------------------------------------------------");
        System.out.println("Enter 1 to generate tables and save data to mongo DB," + newLine +
                "Enter 2 for reading ontology with SPARQL queries," + newLine +
                "Enter 3 to insert new instance(individual) into the ontology");
        System.out.println("-------------------------------------------------------");
        System.out.print("Your Input: ");
        Scanner scanner = new Scanner(System.in);

        return scanner.nextInt();
    }

    private static String processString(String inputString, String namespace) {
        return StringUtils.remove(inputString, namespace);
    }

    private static void generateTripleStoreTable(List<String> objectPropertiesInput, List<String> domainsInput,
                                                 List<String> rangesInput) {
        AsciiTable tripleStoreTable = new AsciiTable();

        tripleStoreTable.addRule();
        tripleStoreTable.addRow("Subject", "Predicate", "Object");
        tripleStoreTable.addRule();

        for (int counter = 0; counter < objectPropertiesInput.size(); counter++) {
            tripleStoreTable.addRow(domainsInput.get(counter), objectPropertiesInput.get(counter),
                    rangesInput.get(counter));
        }

        tripleStoreTable.addRule();
        String renderTripleStoreTable = tripleStoreTable.render();
        System.out.println(renderTripleStoreTable);
    }

    private static void outputType1(String location) throws ParserConfigurationException {
        // Extract namespace using Jena
        InputStream input = FileManager.get().open(location);
        Model model1 = FileManager.get().loadModel(location);

        String namespace = model1.getNsPrefixURI("");

        // Extract tags and attributes using DOMParser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        List<Individual> individuals = new ArrayList<Individual>();

        try {
            Document document = builder.parse(input);
            document.getDocumentElement().normalize();

            List<String> objectProperties = new ArrayList<String>();
            List<String> dataProperties = new ArrayList<String>();
            List<String> domains = new ArrayList<String>();
            List<String> ranges = new ArrayList<String>();

            NodeList objectPropertyList = document.getElementsByTagName("owl:ObjectProperty");

            for (int temp = 0; temp < objectPropertyList.getLength(); temp++) {
                // Retrieves the ObjectProperties (Predicates)
                Node node = objectPropertyList.item(temp);
                Element objectPropertyElement = (Element) node;
                String tempObjectProperty = processString(objectPropertyElement.getAttribute("rdf:about"),
                        namespace);
                objectProperties.add(tempObjectProperty);

                // Retrieves the Domains (Subject)
                NodeList domainList = objectPropertyElement.getElementsByTagName("rdfs:domain");
                Node domainNode = domainList.item(0);
                Element domainElement = (Element) domainNode;
                String tempDomain = processString(domainElement.getAttribute("rdf:resource"), namespace);
                domains.add(tempDomain);

                // Retrieves the Ranges (Object)
                NodeList rangeList = objectPropertyElement.getElementsByTagName("rdfs:range");
                Node rangeNode = rangeList.item(0);
                Element rangeElement = (Element) rangeNode;
                String tempRange = processString(rangeElement.getAttribute("rdf:resource"), namespace);
                ranges.add(tempRange);
            }

            generateTripleStoreTable(objectProperties, domains, ranges);


            // Retrieves Data Properties
            NodeList dataPropertyList = document.getElementsByTagName("owl:DatatypeProperty");

            for (int temp = 0; temp < dataPropertyList.getLength(); temp++) {
                Node node = dataPropertyList.item(temp);
                Element dataPropertyElement = (Element) node;
                String tempDataProperty = processString(dataPropertyElement.getAttribute("rdf:about"), namespace);
                dataProperties.add(tempDataProperty);
            }

            NodeList individualsList = document.getElementsByTagName("owl:NamedIndividual");

            AsciiTable firstOrderLogicTable = new AsciiTable();
            firstOrderLogicTable.addRule();
            firstOrderLogicTable.addRow("Property Type", "Value", "Subject", "Object");
            firstOrderLogicTable.addRule();

            Individual individual;// = null;

            for (int temp = 0; temp < individualsList.getLength(); temp++) {

                String firstOrderLogicTableRowPropertyType;
                String firstOrderLogicTableRowValue;
                String firstOrderLogicTableRowSubject;
                String firstOrderLogicTableRowObject;

                Node node = individualsList.item(temp);
                Element individualElement = (Element) node;
                firstOrderLogicTableRowSubject = processString(individualElement.getAttribute("rdf:about"),
                        namespace);

                individual = new Individual(firstOrderLogicTableRowSubject);
                List<String> objectPropertyObjects = new ArrayList<String>();

                // Extract the Object property related elements from individuals
                for (String objectProperty : objectProperties) {
                    firstOrderLogicTableRowPropertyType = "Object";
                    firstOrderLogicTableRowValue = objectProperty;
                    NodeList individualObjectPropertiesList = individualElement.getElementsByTagName(objectProperty);
                    for (int counter = 0; counter < individualObjectPropertiesList.getLength(); counter++) {
                        Node individualObjectPropertyNode = individualObjectPropertiesList.item(counter);
                        Element individualObjectPropertyElement = (Element) individualObjectPropertyNode;
                        firstOrderLogicTableRowObject = processString(individualObjectPropertyElement.getAttribute("rdf" +
                                ":resource"), namespace);

                        firstOrderLogicTable.addRow(firstOrderLogicTableRowPropertyType, firstOrderLogicTableRowValue
                                , firstOrderLogicTableRowSubject, firstOrderLogicTableRowObject);

                        objectPropertyObjects.add(firstOrderLogicTableRowObject);
                    }
                    if (objectPropertyObjects.size() > 0) {
                        individual.objectProperties.put(firstOrderLogicTableRowValue, objectPropertyObjects);
                    }
                }

                // Extract the Data property related elements from individuals
                for (String dataProperty : dataProperties) {
                    firstOrderLogicTableRowPropertyType = "Data";
                    firstOrderLogicTableRowValue = dataProperty;
                    NodeList individualDataPropertiesList = individualElement.getElementsByTagName(dataProperty);
                    if (individualDataPropertiesList.getLength() > 0) {
                        Node individualDataPropertyNode = individualDataPropertiesList.item(0);
                        firstOrderLogicTableRowObject = individualDataPropertyNode.getFirstChild().getTextContent();

                        firstOrderLogicTable.addRow(firstOrderLogicTableRowPropertyType, firstOrderLogicTableRowValue
                                , firstOrderLogicTableRowSubject, firstOrderLogicTableRowObject);

                        individual.dataProperties.put(firstOrderLogicTableRowValue, firstOrderLogicTableRowObject);
                    }
                }

                individuals.add(individual);
            }

            firstOrderLogicTable.addRule();
            String renderFirstOrderLogicTable = firstOrderLogicTable.render();
            System.out.println(renderFirstOrderLogicTable);

            System.out.print("Do you want to save the extracted individuals information to MongoDB? " +
                    "(Y for Yes, N for No) : ");
            Scanner scanner = new Scanner(System.in);
            String saveToDBPrompt = scanner.next();

            if (saveToDBPrompt.equals("Y")) {
                saveToDB(individuals);
            }

        } catch (SAXException e) {
            System.out.println("Error reading RDF: " + e);
        } catch (IOException e) {
            System.out.println("Error reading RDF: " + e);
        }
    }

    /*
    Code related to saving the data extracted from the RDF file given by the user
    */
    private static void saveToDB(List<Individual> individuals) {

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("ontology");
        MongoCollection<org.bson.Document> collection = database.getCollection("university");

        for (Individual individual : individuals) {
            org.bson.Document document = new org.bson.Document("name", individual.getName());

            if (individual.objectProperties.size() > 0) {
                for (Map.Entry<String, List<String>> objectProperty : individual.objectProperties.entrySet()) {
                    document.append(objectProperty.getKey(), objectProperty.getValue());
                }
            }
            if (individual.dataProperties.size() > 0) {
                for (Map.Entry<String, String> dataProperty : individual.dataProperties.entrySet()) {
                    document.append(dataProperty.getKey(), dataProperty.getValue());
                }
            }
            collection.insertOne(document);
        }
    }

}

