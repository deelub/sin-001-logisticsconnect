package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class HubServiceApp {

    public record HubRecord (String hubID, String province, String hubName , String flag){}

    private static List<HubRecord> cleanedList = new ArrayList<>();

    private static void getCleanedData() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7050/hubs"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                cleanedList = mapper.readValue(
                        response.body(),
                        new TypeReference<List<HubRecord>>() {
                        }
                );
                System.out.println("Successfully fetched " + cleanedList.size() + " hub details ");
            } else {
                System.err.println("Failed to fetch Hub details. HTTP status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching canonical data from ingestion-service: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        getCleanedData();
        Javalin app = Javalin.create().start(7051);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get ("hubs/{id}", ctx -> {

            String id= ctx.pathParam("id");

            if (cleanedList.isEmpty()){
                getCleanedData();
            }

            HubRecord foundRecord = cleanedList.stream()
                    .filter(record -> record.hubID().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (foundRecord != null) {
                ctx.json(foundRecord);
            } else {
                ctx.status(404).result("Intersection not found");
            }
        });

        // TODO (Serves provinces and sorting centers (place-name source of truth).)
        // Add domain endpoints for hub-service here.
    }
}
