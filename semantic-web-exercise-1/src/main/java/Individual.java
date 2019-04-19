import java.util.HashMap;
import java.util.List;

/*
Data related to extracted individuals
 */
class Individual {

    private String name;

    HashMap<String, List<String>> objectProperties = new HashMap<String, List<String>>();

    HashMap<String, String> dataProperties = new HashMap<String, String>();


    Individual(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

}