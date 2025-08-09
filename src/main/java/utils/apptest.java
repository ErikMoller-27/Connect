package utils;

import utils.textextractor.ExtractionResult;
import DAO.userdao;

import java.nio.file.Path;
import java.nio.file.Paths;
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

        // 3) AI call (Gemini) + DB save + optional radar chart
        try {
            // debug: confirm env is visible
            String keySet = System.getenv("GEMINI_API_KEY");
            System.out.println("\n[debug] gemini key configured: " + (keySet != null && !keySet.isBlank()));

            var ai = new aiclientgemini();

            // fixed, future-proof keys
            List<String> keywords = List.of(
                    "Education",
                    "ProgrammingSkills",
                    "Certifications",
                    "Projects",
                    "Collaboration",
                    "Experience"
            );

            String userIdName = "demo-user"; // TODO: wire to real account/display name
            String jobId      = "demo-job";  // optional

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

            dao.replaceSkills(userId, six);
            System.out.println("[db] replaced 6 skills for userId=" + userId);

            // optional log-friendly CSV snapshot
            String subjectsCsv = String.join(",", six.keySet());
            String valuesCsv = six.values().stream().map(String::valueOf)
                    .reduce((a,b) -> a + "," + b).orElse("");
            System.out.println("[snapshot] " + subjectsCsv + " = " + valuesCsv);

            // ---- Radar PNG (only if utils.radarimage is present)
            saveRadarIfAvailable(six, userId);

        } catch (Exception e) {
            System.out.println("\nAI/DB step failed: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    // Use reflection so this compiles even if utils.radarimage isn't added yet.
    private static void saveRadarIfAvailable(Map<String,Integer> six, int userId) {
        try {
            Class<?> cls = Class.forName("utils.radarimage");
            var m = cls.getMethod("saveRadarPng", Map.class, int.class, Path.class);
            Path outPng = Paths.get("target", "radar-" + userId + ".png");
            m.invoke(null, six, 640, outPng);
            System.out.println("[chart] wrote " + outPng.toAbsolutePath());
        } catch (ClassNotFoundException e) {
            System.out.println("[chart] utils.radarimage not found; skip chart generation.");
        } catch (NoSuchMethodException e) {
            System.out.println("[chart] saveRadarPng(Map,int,Path) not found; skip chart generation.");
        } catch (Throwable t) {
            System.out.println("[chart] failed: " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }
}
