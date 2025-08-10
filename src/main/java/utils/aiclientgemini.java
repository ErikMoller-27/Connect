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
        return """
You are scoring a candidate based ONLY on the resume/cover-letter text below.

Return ONLY valid JSON with EXACTLY these keys and integer values 0–100:
{
"scores": {
"Education": 0-100,
"ProgrammingSkills": 0-100,
"Certifications": 0-100,
"Projects": 0-100,
"Collaboration": 0-100,
"Experience": 0-100
}
}
Requirements:
- Output JSON only (no prose, no code fences).
- Include ALL six keys even if a score is 0.
- Do NOT add, remove, rename, or nest keys.
- Use integers 0–100 with FINE-GRAINED values (e.g., 17, 34, 62, 98).
- Do NOT default to anchor values {0, 25, 50, 75, 100} unless the evidence lands EXACTLY on an anchor.

Scoring method (apply to EACH key):
1) Estimate an evidence strength E in [0,1] using:
- Specificity/clarity of claims (concrete roles, tools, outcomes)
- Quantity/density of relevant evidence across the document
- Recency/currency where applicable
- Scale/impact when numbers are provided (GPA, users, uptime, latency, etc.)
2) Convert to a continuous score S = round(100 * E).
3) Apply small adjustments (±1–10) for:
+ Quantified outcomes, awards/honours, promotions, advanced credentials
− Vague wording, lack of recency, unclear scope/impact
4) Result must be an integer 0–100 (not restricted to multiples of 5).

Calibration bands (guidance, NOT hard buckets):
- 0–9: no substantive evidence
- 10–24: weak/isolated mentions
- 25–49: some concrete evidence; limited depth/scope
- 50–74: solid, recurring evidence; good relevance
- 75–89: strong, sustained evidence with outcomes
- 90–100: exceptional breadth/depth and clearly quantified impact

Definitions & guardrails:

- Education:
Degree level, GPA/%, honours/scholarships, relevant coursework/research/thesis.
If a top-tier institution is explicitly named, treat as stronger evidence; do NOT guess ranking if unstated.

- ProgrammingSkills:
Breadth/depth of coding ability and currency (versions/paradigms). Evidence: languages, frameworks, tools, repos/competitions, complexity tackled.
Do NOT credit certifications here unless there is applied skill evidence in text.

- Certifications:
Recognized credentials (vendor + level + year) and relevance. Do NOT infer mastery beyond what’s stated.

- Projects:
Discrete initiatives (academic/personal/OSS/enterprise) with scope, stack, ownership, deployment/real users, and results.
Anti–double-count: impact/complexity goes here; tenure/title stays under Experience.

- Collaboration:
Teamwork and stakeholder interaction: cross-functional work, docs, presentations/demos, mentorship, reviews.
Mere team membership without evidence of contribution is weak.

- Experience:
Roles/titles, duration/continuity, progression, domain relevance, scale (users/clients/regions).
Anti–double-count: focus on tenure/scope/responsibilities; don’t re-count project impact.

General rules:
- Use ONLY the supplied text; be conservative when ambiguous.
- Prefer granular integers over anchors; avoid rounding to 0/25/50/75/100 unless truly warranted.
- Be strict, like really strict
""";
    }



    private String safe_text(String t) {
        if (t == null) return "";
        int max = 60000;
        return (t.length() <= max) ? t : t.substring(0, max);
    }
}
