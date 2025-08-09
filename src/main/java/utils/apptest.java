package utils;

import utils.textextractor.ExtractionResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class apptest {
    public static void main(String[] args) {
        // 1) Pick files
        System.out.println("opening file chooser...");
        List<Path> files = fileuploader.pickFiles();
        System.out.println("chooser closed. selected: " + files.size() + " file(s).");
        if (files.isEmpty()) {
            System.out.println("no files selected.");
            return;
        }

        // 2) Extract text
        textextractor extractor = new textextractor();
        ExtractionResult result = extractor.extract(files);

        // per-file status
        result.files().forEach(f ->
                System.out.println((f.succeeded() ? "✅" : "❌") + " " + f.name +
                        (f.succeeded() ? (" (" + f.sizeBytes + " bytes)") : (" | " + f.error)))
        );

        // quick preview
        String text = result.text();
        int len = text.length();
        int head = Math.min(800, len);
        System.out.println("\n--- PREVIEW (first " + head + " chars) ---\n");
        if (head > 0) System.out.println(text.substring(0, head));

        // 3) AI call (Gemini)
        try {
            // debug: confirm env is visible
            String keySet = System.getenv("GEMINI_API_KEY");
            System.out.println("\n[debug] gemini key configured: " + (keySet != null && !keySet.isBlank()));

            var ai = new aiclientgemini();
            List<String> keywords = List.of(
                    "Leadership","Communication","Technical","ProblemSolving","Adaptability","Innovation"
            );

            String userId = "demo-user";
            String jobId  = "demo-job";

            Map<String,Integer> scores = ai.score(userId, jobId, text, keywords);

            System.out.println("\nAI scores:");
            scores.forEach((k,v) -> System.out.println(" - " + k + ": " + v + "%"));
        } catch (Exception e) {
            System.out.println("\nAI call failed: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
