package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class aiclienthttp {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Configure via env vars (IntelliJ: Run Config → Environment variables)
    private final String apiUrl = System.getenv().getOrDefault("AI_URL", "http://localhost:8000/score");
    private final String apiKey = System.getenv().getOrDefault("AI_KEY", "");

    /** POSTs text + keywords → returns 0–100 scores per keyword. */
    public Map<String, Integer> score(String userId, String jobId, String text, List<String> keywords) {
        try {
            String body = MAPPER.writeValueAsString(Map.of(
                    "userId", userId,
                    "jobId", jobId,
                    "text", safeText(text),
                    "keywords", keywords
            ));

            HttpRequest.Builder rb = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json");
            if (!apiKey.isEmpty()) rb.header("Authorization", "Bearer " + apiKey);

            HttpRequest req = rb.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("AI HTTP " + resp.statusCode() + ": " + resp.body());
            }

            // Expect: {"scores": {"Leadership":72, ...}}
            Map<String, Object> root = MAPPER.readValue(resp.body(), new TypeReference<>() {});
            Object scoresObj = root.get("scores");
            if (!(scoresObj instanceof Map)) throw new RuntimeException("Invalid response: no 'scores' field");

            Map<?, ?> raw = (Map<?, ?>) scoresObj;
            Map<String, Integer> out = new LinkedHashMap<>();
            for (String k : keywords) {
                Object v = raw.get(k);
                if (v == null) continue;
                int iv = (v instanceof Number) ? ((Number) v).intValue() : Integer.parseInt(v.toString());
                if (iv < 0) iv = 0; if (iv > 100) iv = 100;
                out.put(k, iv);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("AI call failed: " + e.getMessage(), e);
        }
    }

    private String safeText(String t) {
        if (t == null) return "";
        int max = 60_000; // keep payload snappy
        return (t.length() <= max) ? t : t.substring(0, max);
    }
}
