package utils;

import utils.textextractor.ExtractionResult;
import DAO.userdao;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

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

        // 3) AI call (Gemini) + DB save + show animation
        try {
            // debug: confirm env is visible
            String keySet = System.getenv("GEMINI_API_KEY");
            System.out.println("\n[debug] gemini key configured: " + (keySet != null && !keySet.isBlank()));

            var ai = new aiclientgemini();

            // fixed keys
            List<String> keywords = List.of(
                    "Education",
                    "ProgrammingSkills",
                    "Certifications",
                    "Projects",
                    "Collaboration",
                    "Experience"
            );

            // identifiers (swap to your real logged-in user/job later)
            String userIdName = "demo-user";
            String jobId      = "demo-job";

            Map<String,Integer> scores = ai.score(userIdName, jobId, text, keywords);

            System.out.println("\nAI scores:");
            scores.forEach((k,v) -> System.out.println(" - " + k + ": " + v + "%"));
            if (scores.size() != 6) {
                System.out.println("[warn] expected 6 keys, got " + scores.size());
            }

            // ---- DB: replace existing skills (no duplicates)
            userdao dao = new userdao(); // uses jdbc:sqlite:main.db
            dao.initializeTables();

            int userId = dao.getUserIdByName(userIdName);
            if (userId < 0) userId = dao.createUser(userIdName);

            // enforce exactly-six map in a stable order
            Map<String,Integer> six = new LinkedHashMap<>();
            six.put("Education",          scores.getOrDefault("Education", 0));
            six.put("ProgrammingSkills",  scores.getOrDefault("ProgrammingSkills", 0));
            six.put("Certifications",     scores.getOrDefault("Certifications", 0));
            six.put("Projects",           scores.getOrDefault("Projects", 0));
            six.put("Collaboration",      scores.getOrDefault("Collaboration", 0));
            six.put("Experience",         scores.getOrDefault("Experience", 0));


            // show the animated radar and WAIT until the window is closed
            utils.fxradarwindow.showAndWait(six);

            // optional log-friendly CSV snapshot
            String subjectsCsv = String.join(",", six.keySet());
            String valuesCsv = six.values().stream().map(String::valueOf)
                    .reduce((a,b) -> a + "," + b).orElse("");
            System.out.println("[snapshot] " + subjectsCsv + " = " + valuesCsv);

        } catch (Exception e) {
            System.out.println("\nAI/DB step failed: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
