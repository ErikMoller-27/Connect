package utils;

import java.util.List;
import java.util.Map;

public class AIClientWrapper {
    private final aiclientgemini geminiClient = new aiclientgemini();

    public Map<String, Integer> getScores(String userId, String jobId, String text, List<String> keywords) {
        return geminiClient.score(userId, jobId, text, keywords);
    }
}
