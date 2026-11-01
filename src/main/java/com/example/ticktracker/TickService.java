package com.example.ticktracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TickService {
    private final String apiUrl = "https://dev-task.elancoapps.com/data/tick-sightings";
    private final RestTemplate client = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private List<TickSighting> saved = null;
    private long lastDownload = 0;
    private int duplicates = 0;
    private int invalid = 0;
    private int received = 0;

    // Download the data, or reuse it if we downloaded it recently.
    public synchronized List<TickSighting> getSightings() {
        long now = System.currentTimeMillis();
        if (saved != null && now - lastDownload < 300000) {
            return saved;
        }

        try {
            String json = client.getForObject(apiUrl, String.class);
            JsonNode rows = mapper.readTree(json); //converts the son string in a format Java can navigate

            if (rows == null || !rows.isArray()) {
                throw new Exception("Invalid data from API");
            }

            saved = cleanData(rows);
            lastDownload = now;
            return saved;

        } catch (Exception e) {
            // If Elanco is unavailable, we can still show earlier data.
            if (saved != null) {
                return saved;
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not get data from Elanco");
        }
    }

    // Check the records and remove duplicates or incomplete entries.
    List<TickSighting> cleanData(JsonNode rows) {
        List<TickSighting> list = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        received = rows.size();
        duplicates = 0;
        invalid = 0;

        for (JsonNode row : rows) {
            String id = row.path("id").asText("").trim();
            String date = row.path("date").asText("").trim();
            String city = row.path("location").asText("").trim();
            String species = row.path("species").asText("").trim();
            String latin = row.path("latinName").asText("").trim();

            if (id.isEmpty() || date.isEmpty() || city.isEmpty()
                    || species.isEmpty() || latin.isEmpty()) {
                invalid++;
                continue;
            }

            try {
                LocalDateTime.parse(date);
            } catch (Exception e) {
                invalid++;
                continue;
            }

            if (ids.contains(id)) {
                duplicates++;
                continue;
            }

            ids.add(id);
            list.add(new TickSighting(id, date, city, species, latin));
        }
        return list;
    }

    public int getReceived() { return received; }
    public int getDuplicates() { return duplicates; }
    public int getInvalid() { return invalid; }
}
