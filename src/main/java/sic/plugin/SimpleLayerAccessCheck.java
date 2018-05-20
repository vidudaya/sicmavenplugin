package sic.plugin;


import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class SimpleLayerAccessCheck {

    private static Map<String, SimpleLayer> layerRulesMap = new HashMap<String, SimpleLayer>();

    public static boolean start(RootDoc root) throws IOException, ParseException {
        processRules();
        processLayers(root.specifiedPackages());
        return true;
    }

    private static void processLayers(PackageDoc[] packages) {
        StringBuilder sb = new StringBuilder();
        for (PackageDoc layer : packages) {
            String nameSplit[] = layer.name().split("\\.");
            String layerName = nameSplit[nameSplit.length - 1];
            System.out.println("\nAnalyzing classes of " + layerName + " layer");

            SimpleLayer layerRule = layerRulesMap.get(layerName);

            ClassDoc[] classes = layer.allClasses();
            boolean isPass;
            for (ClassDoc classDoc : classes) {
                isPass = true;
                Tag[] serverTags = classDoc.tags("sic.server");
                Tag[] clientTags = classDoc.tags("sic.client");

                for (Tag tag : serverTags) {
                    if (tag.text().length() > 0 && !tag.text().equals(layerRule.getServer())) {
                        System.out.println("\t[Error - Layer Invariants Violation] " + classDoc.name()
                                + " violates the layered architecture by accessing " + tag.text() + " as a client");
                        isPass = false;
                        sb.append("\t").append(classDoc.name()).append(" violates the layered architecture by accessing ").append(tag.text()).append(" as a client\n");
                    }
                }

                for (Tag tag : clientTags) {
                    if (tag.text().length() > 0 && !tag.text().equals(layerRule.getClient())) {
                        System.out.println("\t[Error - Layer Invariants Violation] " + classDoc.name()
                                + " violates the layered architecture by accessing " + tag.text() + " as a server");
                        isPass = false;
                        sb.append("\t").append(classDoc.name()).append(" violates the layered architecture by accessing ").append(tag.text()).append(" as a server\n");
                    }
                }

                if (isPass) {
                    System.out.println("\tClass " + classDoc.name() + " Layer invariants check PASSED");
                } else {
                    Support.isBuildPassed = false;
                    Support.buildFailureMsg = "layered architecture violations detected !!!\n".concat(sb.toString());
                }
            }
        }
    }

    private static void processRules() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        //Use JSONObject for simple JSON and JSONArray for array of JSON.
        JSONObject data = (JSONObject) parser.parse(
                new FileReader(Support.layerRuleFileLocation));//path to the JSON file.


        String json = data.toJSONString();

        JSONArray msg = (JSONArray) data.get("layers");
        for (JSONObject layerJson : (Iterable<JSONObject>) msg) {
            String layerName = (String) layerJson.get("name");
            String serverLayer = (String) layerJson.get("server");
            String clientLayer = (String) layerJson.get("client");

            SimpleLayer layer = new SimpleLayer(layerName, serverLayer, clientLayer);
            layerRulesMap.put(layerName, layer);
        }
    }
}

class SimpleLayer {
    private String name;
    private String server;
    private String client;

    SimpleLayer(String name, String server, String client) {
        this.name = name;
        this.server = server;
        this.client = client;
    }

    String getServer() {
        return server;
    }

    String getClient() {
        return client;
    }

    @Override
    public String toString() {
        return "SimpleLayer{" +
                "name='" + name + '\'' +
                ", server='" + server + '\'' +
                ", client='" + client + '\'' +
                '}';
    }
}

@SuppressWarnings("unused")
class Layer {
    private String name;
    private List<Layer> servers;
    private List<Layer> clients;

    public Layer(String name) {
        this.name = name;
        this.servers = new ArrayList<Layer>();
        this.clients = new ArrayList<Layer>();
    }

    public String getName() {
        return name;
    }

    public List<Layer> getServers() {
        return servers;
    }

    public List<Layer> getClients() {
        return clients;
    }

    public void addServer(Layer serverLayer) {
        this.servers.add(serverLayer);
    }

    public void addClient(Layer clientLayer) {
        this.clients.add(clientLayer);
    }
}