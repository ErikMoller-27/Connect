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

public class aiclientgemini {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // set in Run Config → Environment variables:
    // GEMINI_API_KEY=xxxx                 (required)
    // GEMINI_MODEL=gemini-1.5-flash       (optional; default below)
    private final String apiKey  = System.getenv().getOrDefault("GEMINI_API_KEY", "");
    private final String modelId = System.getenv().getOrDefault("GEMINI_MODEL", "gemini-1.5-flash");
    private final String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
            + modelId + ":generateContent";

    /** Calls Gemini and returns 0–100 scores per keyword. */
    public Map<String, Integer> score(String userId, String jobId, String text, List<String> keywords) {
        if (apiKey.isBlank()) throw new IllegalStateException("GEMINI_API_KEY not set");
        try {
            String promptHeader = build_prompt(keywords);
            String idBlock = "USER_ID: " + userId + (jobId == null ? "" : "\nJOB_ID: " + jobId);
            String fullPrompt = promptHeader + "\n" + idBlock + "\nTEXT:\n" + safe_text(text);

            String payload = MAPPER.writeValueAsString(Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", fullPrompt))
                    )),
                    // force JSON-only output so parsing is reliable
                    "generationConfig", Map.of("response_mime_type", "application/json")
            ));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("gemini http " + resp.statusCode() + ": " + resp.body());
            }

            // extract JSON text from candidates[0].content.parts[0].text
            Map<String, Object> root = MAPPER.readValue(resp.body(), new TypeReference<>() {});
            List<?> candidates = (List<?>) root.get("candidates");
            if (candidates == null || candidates.isEmpty())
                throw new RuntimeException("no candidates from gemini");

            @SuppressWarnings("unchecked")
            Map<String, Object> first = (Map<String, Object>) candidates.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) first.get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty())
                throw new RuntimeException("no parts in gemini response");

            String textOut = String.valueOf(parts.get(0).get("text"));
            if (textOut == null) throw new RuntimeException("empty text in gemini response");

            Map<String, Object> parsed = MAPPER.readValue(textOut, new TypeReference<>() {});
            Object scoresObj = parsed.get("scores");
            if (!(scoresObj instanceof Map))
                throw new RuntimeException("json has no 'scores' field");

            Map<?, ?> raw = (Map<?, ?>) scoresObj;
            Map<String, Integer> out = new LinkedHashMap<>();
            for (String k : keywords) {
                Object v = raw.get(k);
                if (v == null) continue;
                int iv = (v instanceof Number) ? ((Number) v).intValue() : Integer.parseInt(v.toString());
                if (iv < 0) iv = 0;
                if (iv > 100) iv = 100;
                out.put(k, iv);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("gemini call failed: " + e.getMessage(), e);
        }
    }

    private String build_prompt(List<String> keywords) {
        return "You are scoring a candidate based on resume/cover letter text.\n"
                + "Return ONLY valid JSON with this exact shape:\n"
                + "{ \"scores\": {\"KEY\": 0-100, ...} }\n"
                + "Use ONLY these keys exactly as spelled: " + keywords + "\n"
                + "Rubric:\n"
                + "- Leadership: leads people/outcomes.\n"
                + "- Communication: writing, presentations, stakeholder updates.\n"
                + "- Technical: tools, languages, systems.\n"
                + "- ProblemSolving: debugging, analysis, optimisation.\n"
                + "- Adaptability: learning new tools, switching contexts.\n"
                + "- Innovation: initiating improvements/new ideas.\n"
                + "Respond with integers 0–100 for each key.";
    }

    private String safe_text(String t) {
        if (t == null) return "";
        int max = 60000;
        return (t.length() <= max) ? t : t.substring(0, max);
    }
}
