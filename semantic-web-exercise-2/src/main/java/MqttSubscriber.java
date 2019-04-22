import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

public class MqttSubscriber {

    public static void main(String[] args) {
        // Strat the MQTT broker - brew services start mosquitto
        String topic = "MetaData";
        String broker = "tcp://localhost:1883";
        String clientId = "M002";
        //MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
            sampleClient.subscribe(topic, 1);
            System.out.println("Connected and listening for MetaData...");
            sampleClient.setCallback(new SimpleCallback());
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("except " + me);
            me.printStackTrace();
        }
    }

    static void insertOntology() throws FileNotFoundException {
        String newLine = System.getProperty("line.separator");
        System.out.println("-------------------------------------------------------");
        System.out.println("Enter 1 to save meta data to the ontology \n " +
                "Enter 2 for listen again");
        System.out.println("-------------------------------------------------------");
        System.out.print("Your Input: ");
        Scanner scanner = new Scanner(System.in);
        int userChoice = scanner.nextInt();

        if (userChoice == 1) {


            String location = "/Users/madhawaperera/Documents/GitHub/university.rdf";
            Model m = ModelFactory.createDefaultModel();
            m.read(location);
            String NS = "http://university.owl#";

            Resource r = m.createResource("http://university.owl#Lect3")
                    .addProperty(RDF.type, m.getResource(NS + "Lecturer")); //subject

            Property p1 = m.createProperty(NS + "staff_id");  //predicates
            Property p2 = m.createProperty(NS + "fName");
            Property p3 = m.createProperty(NS + "lName");

            String[] values = SimpleCallback.publishedMessage.split(",");

            r.addProperty(p1, (values[0].split(":"))[1], XSDDatatype.XSDint); // objects
            r.addProperty(p2, (values[1].split(":"))[1], XSDDatatype.XSDstring);
            r.addProperty(p3, (values[2].split(":"))[1], XSDDatatype.XSDstring);

            m.write(new FileOutputStream(location), "RDF/XML-ABBREV");

        } else if (userChoice == 2) {
            System.out.println("");
            System.out.println("Connected and listening again for MetaData...");
        }

    }


//    ToDo: Add loading function to the terminal to show that this program is listening...

//    public static Thread thread = new Thread("Listener") {
//        public void run(){
//            System.out.println("run by: " + getName());
//            char[] animationChars = new char[]{'|', '/', '-', '\\'};
//
//            for (int i = 0; i<=20; i++) {
//                System.out.print("Listening: " + animationChars[i % 4] + "\r");
//
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    };

}

