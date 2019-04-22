import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SimpleCallback implements MqttCallback {

    static String publishedMessage;

    //Called when the client lost the connection to the broker
    public void connectionLost(Throwable cause) {
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("-------------------------------------------------------");
        System.out.println("| Topic: " + topic);
        System.out.println("| Message: " + new String(message.getPayload()));
        publishedMessage = new String(message.getPayload());
        MqttSubscriber.insertOntology();
        System.out.println("-------------------------------------------------------");
    }

    //Called when a outgoing publish is complete
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
