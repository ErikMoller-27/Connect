package models;

import java.util.HashMap;
import java.util.Map;

public class user {
    private final int userId;
    private final String firstName;
    private final Map<String, Integer> skills = new HashMap<>();

    public user(int userId, String firstName) {
        this.userId = userId;
        this.firstName = firstName;
    }

    public void addSkills(Map<String, Integer> skillsToAdd) {
        skills.clear();
        skills.putAll(skillsToAdd);
    }

    public String getFirstName() {
        return firstName;
    }

    public Map<String, Integer> getSkills() {
        return skills;
    }
}
